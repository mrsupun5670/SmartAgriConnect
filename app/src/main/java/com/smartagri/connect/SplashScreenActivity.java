package com.smartagri.connect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainTracking), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView splashLogo = findViewById(R.id.splahLogo);
        TextView splashText = findViewById(R.id.splashText);
        splashText.setVisibility(View.INVISIBLE);


        SpringAnimation springAnimation = new SpringAnimation(splashLogo, SpringAnimation.TRANSLATION_Y, 0f);
        SpringForce springForce = new SpringForce(0)
                .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_VERY_LOW);
        springAnimation.setSpring(springForce);
        splashLogo.setTranslationY(300f);
        springAnimation.start();


        Animation fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.open_ina_nim);
        splashLogo.postDelayed(() -> {
            splashText.setVisibility(View.VISIBLE);
            splashText.startAnimation(fadeIn);
        }, 1200);


        splashLogo.postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            SharedPreferences sharedPreferences = getSharedPreferences("com.smartagri.connect.userdata", MODE_PRIVATE);

            if (currentUser != null && sharedPreferences.contains("user")) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            }
            finish();
        }, 3000);
    }
}
