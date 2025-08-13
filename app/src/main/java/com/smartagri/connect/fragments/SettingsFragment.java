package com.smartagri.connect.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.FrameLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.smartagri.connect.R;

public class SettingsFragment extends Fragment {

    private boolean isOn = false;

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

        ConstraintLayout customSwitch = view.findViewById(R.id.customSwitch);
        TextView switchText = view.findViewById(R.id.switchText);

        customSwitch.setOnClickListener(v -> {
            isOn = !isOn;



            if (isOn) {
                switchText.setText("අ");
                switchText.animate()
                        .translationX(customSwitch.getWidth() - switchText.getWidth()-18)
                        .setDuration(200)
                        .start();

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(0xFF4CC417); // Purple
                drawable.setCornerRadius(50f); // Rounded corners
                customSwitch.setBackground(drawable);
            } else {
                switchText.setText("A");
                switchText.animate()
                        .translationX(0)
                        .setDuration(200)
                        .start();

                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(0xFF525252); // Dark gray
                drawable.setCornerRadius(50f); // Rounded corners
                customSwitch.setBackground(drawable);
            }

        });

        return view;
    }

}
