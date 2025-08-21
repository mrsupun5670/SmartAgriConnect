package com.smartagri.connect;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.smartagri.connect.helper.LocaleHelper;

public abstract class BaseFragment extends Fragment {
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.onAttach(context));
    }
}
