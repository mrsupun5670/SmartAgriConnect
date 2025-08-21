package com.smartagri.connect;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.smartagri.connect.helper.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
