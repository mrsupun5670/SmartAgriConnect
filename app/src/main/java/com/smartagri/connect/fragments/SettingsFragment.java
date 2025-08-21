package com.smartagri.connect.fragments;

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

import com.smartagri.connect.BaseFragment;
import com.smartagri.connect.R;
import com.smartagri.connect.helper.LocaleHelper;

public class SettingsFragment extends BaseFragment {

    private boolean isOn = false;
    private boolean isChangingLanguage = false; // Flag to prevent multiple clicks
    ConstraintLayout customSwitch;
    TextView switchText;

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

        // Load current language state when fragment is created
        loadLanguageState();

        customSwitch.setOnClickListener(v -> {
            // Prevent multiple clicks during language change
            if (isChangingLanguage) {
                return;
            }

            isChangingLanguage = true; // Set flag to prevent multiple clicks
            isOn = !isOn;

            if (isOn) {
                switchText.setText("අ");
                switchText.animate()
                        .translationX(customSwitch.getWidth() - switchText.getWidth()-18)
                        .setDuration(200)
                        .start();

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(0xFF4CC417); // Green
                drawable.setCornerRadius(50f);
                customSwitch.setBackground(drawable);

                // Save state before changing language
                saveSwitchState(true);
                new Handler().postDelayed(() -> changeLanguage("si"), 300);

            } else {
                switchText.setText("A");
                switchText.animate()
                        .translationX(0)
                        .setDuration(200)
                        .start();

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(0xFF525252); // Dark gray
                drawable.setCornerRadius(50f);
                customSwitch.setBackground(drawable);

                // Save state before changing language
                saveSwitchState(false);
                new Handler().postDelayed(() -> changeLanguage("en"), 300);
            }
        });

        return view;
    }

    private void changeLanguage(String languageCode) {
        // Update locale
        LocaleHelper.setLocale(requireContext(), languageCode);

        // Restart the entire activity to apply language changes
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);

        // Add smooth transition
        requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
    }

    private void loadLanguageState() {
        // Get current language from LocaleHelper
        String currentLang = LocaleHelper.getLanguage(requireContext());

        // Also check SharedPreferences for the most recent switch state
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isSinhalaSelected = prefs.getBoolean("is_sinhala_selected", currentLang.equals("si"));

        // Set switch state based on saved preference
        if (isSinhalaSelected) {
            isOn = true;
            switchText.setText("අ");

            // Set switch position (use post to ensure view is measured)
            customSwitch.post(() -> {
                switchText.setTranslationX(customSwitch.getWidth() - switchText.getWidth()-18);
            });

            // Set background color
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(0xFF4CC417);
            drawable.setCornerRadius(50f);
            customSwitch.setBackground(drawable);

        } else {
            isOn = false;
            switchText.setText("A");
            switchText.setTranslationX(0);

            // Set background color
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
}