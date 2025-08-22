package com.smartagri.connect.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.smartagri.connect.BaseFragment;
import com.smartagri.connect.LoginActivity;
import com.smartagri.connect.R;
import com.smartagri.connect.helper.LocaleHelper;
import com.smartagri.connect.model.User;

public class SettingsFragment extends BaseFragment {

    private boolean isOn = false;
    private boolean isChangingLanguage = false;
    ConstraintLayout customSwitch;
    TextView switchText, signedUserName, signOut;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        customSwitch = view.findViewById(R.id.customSwitch);
        switchText = view.findViewById(R.id.switchText);
        signedUserName = view.findViewById(R.id.signed_user_name);
        signOut = view.findViewById(R.id.Signed_Logout);

        // Load current language state
        loadLanguageState();

        // Show logged in user name
        SharedPreferences sp = requireActivity().getSharedPreferences("com.smartagri.connect.userdata", Context.MODE_PRIVATE);
        String userJson = sp.getString("user", null);
        if (userJson != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
            if (user != null && user.getName() != null) {
                signedUserName.setText(user.getName());
            } else {
                signedUserName.setText("Guest");
            }
        } else {
            signedUserName.setText("Guest");
        }

        // Language toggle
        customSwitch.setOnClickListener(v -> {
            if (isChangingLanguage) return;
            isChangingLanguage = true;
            isOn = !isOn;

            if (isOn) {
                switchText.setText("අ");
                switchText.animate()
                        .translationX(customSwitch.getWidth() - switchText.getWidth() - 18)
                        .setDuration(200)
                        .start();

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(0xFF4CC417); // Green
                drawable.setCornerRadius(50f);
                customSwitch.setBackground(drawable);

                saveSwitchState(true);
                new Handler().postDelayed(() -> changeLanguage("si"), 300);

            } else {
                switchText.setText("A");
                switchText.animate()
                        .translationX(0)
                        .setDuration(200)
                        .start();

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(0xFF525252); // Gray
                drawable.setCornerRadius(50f);
                customSwitch.setBackground(drawable);

                saveSwitchState(false);
                new Handler().postDelayed(() -> changeLanguage("en"), 300);
            }
        });

        // Logout click
        signOut.setOnClickListener(v -> showLogoutConfirmationDialog());

        return view;
    }

    private void changeLanguage(String languageCode) {
        LocaleHelper.setLocale(requireContext(), languageCode);
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadLanguageState() {
        String currentLang = LocaleHelper.getLanguage(requireContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isSinhalaSelected = prefs.getBoolean("is_sinhala_selected", currentLang.equals("si"));

        if (isSinhalaSelected) {
            isOn = true;
            switchText.setText("අ");
            customSwitch.post(() -> switchText.setTranslationX(customSwitch.getWidth() - switchText.getWidth() - 18));
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(0xFF4CC417);
            drawable.setCornerRadius(50f);
            customSwitch.setBackground(drawable);
        } else {
            isOn = false;
            switchText.setText("A");
            switchText.setTranslationX(0);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(0xFF525252);
            drawable.setCornerRadius(50f);
            customSwitch.setBackground(drawable);
        }
    }

    private void saveSwitchState(boolean isSinhala) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_sinhala_selected", isSinhala);
        editor.apply();
    }

    private void logout() {
        // ✅ Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // ✅ Clear SharedPreferences
        SharedPreferences sp = requireActivity().getSharedPreferences("com.smartagri.connect.userdata", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        // ✅ Redirect to LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialogInterface, which) -> logout())
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(android.R.color.holo_green_light));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getResources().getColor(android.R.color.holo_red_light));
    }
}
