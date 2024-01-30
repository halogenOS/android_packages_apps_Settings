package com.android.settings.network;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.android.settings.ButtonBarHandler;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SetupWizardUtils;

import com.google.android.setupdesign.util.ThemeHelper;

public class InternetSetupActivity extends SettingsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(SetupWizardUtils.getTheme(this, getIntent()));
        ThemeHelper.trySetDynamicColor(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void createUiFromIntent(Bundle savedState, Intent intent) {
        intent.putExtra("extra_prefs_show_button_bar", false);
        super.createUiFromIntent(savedState, intent);
    }

    @Override
    protected boolean isToolbarEnabled() {
        // Hide the action bar from this page.
        return false;
    }

    private static final String FRAGMENT_CLASS_NAME = NetworkProviderSetup.class.getName();

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, FRAGMENT_CLASS_NAME);
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return FRAGMENT_CLASS_NAME.equals(fragmentName);
    }
}
