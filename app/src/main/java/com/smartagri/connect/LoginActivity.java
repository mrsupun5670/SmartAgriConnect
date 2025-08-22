package com.smartagri.connect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.smartagri.connect.model.User;

public class LoginActivity extends BaseActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private MaterialButton loginSignupButton;
    private Button loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("com.smartagri.connect.userdata", MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ✅ Check if already logged in
        if (auth.getCurrentUser() != null && sharedPreferences.contains("user")) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginSignupButton = findViewById(R.id.btnSignUp);
        loginButton = findViewById(R.id.loginbtn);

        loginSignupButton.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
            finish();
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInputs(email, password)) {
                checkLogin(email, password);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validateInputs(String emailInput, String passwordInput) {
        if (emailInput.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();

            return false;
        }
        if (passwordInput.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();

            return false;
        }
        return true;
    }

    private void checkLogin(String emailInput, String passwordInput) {
        auth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Fetch user profile from Firestore
                            DocumentReference docRef = db.collection("Users").document(uid);
                            docRef.get().addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    User user = documentSnapshot.toObject(User.class);

                                    // Save in SharedPreferences
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    String userJson = new Gson().toJson(user);
                                    editor.putString("user", userJson);
                                    editor.apply();

                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "User data not found in Firestore", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
