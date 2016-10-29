/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2016 halogenOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.view.RotationPolicy;
import com.android.settings.accessibility.ToggleFontSizePreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;


public class ButtonSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        Indexable {
    private static final String TAG = ButtonSettings.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String
        KEY_LONG_PRESS_HOME_BUTTON_ASSIST =
                    "long_press_home_button_assist",
        KEY_SHOW_NAVBAR =
                    "buttons_show_navbar";
    
    private static final int
        NAVBAR_MUST_SHOW = -2,
        NAVBAR_NOT_SET = -1,
        NAVBAR_DONT_SHOW = 0,
        NAVBAR_SHOW = 1
        ;

    private SwitchPreference
        mLongPressHomeButtonAssistPreference,
        mShowNavbarPreference
        ;
    
    private Handler mHandler;
    private final Runnable resetNavbarToggle = new Runnable() {
        @Override
        public void run() {
            mShowNavbarPreference.setEnabled(true);
        }
    };

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHandler = new Handler();

        addPreferencesFromResource(R.xml.button_settings);

        mLongPressHomeButtonAssistPreference = (SwitchPreference)
                findPreference(KEY_LONG_PRESS_HOME_BUTTON_ASSIST);
        mLongPressHomeButtonAssistPreference.setOnPreferenceChangeListener(this);
        
        mShowNavbarPreference = (SwitchPreference)
                findPreference(KEY_SHOW_NAVBAR);
        
        int nav = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.SHOW_NAVBAR, -1, UserHandle.USER_CURRENT);
        if(nav == NAVBAR_MUST_SHOW)
            removePreference(KEY_SHOW_NAVBAR);
        else mShowNavbarPreference.setOnPreferenceChangeListener(this);
        
        updateState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        updateState(KEY_LONG_PRESS_HOME_BUTTON_ASSIST);
        updateState(KEY_SHOW_NAVBAR);
    }

    private void updateState(String key) {
        if(DEBUG) Log.d(TAG, "Updating state for " + key);
        switch(key) {
            case KEY_LONG_PRESS_HOME_BUTTON_ASSIST:
                mLongPressHomeButtonAssistPreference.setChecked(
                    Settings.System.getInt(getContentResolver(),
                        Settings.System.LONG_PRESS_HOME_BUTTON_BEHAVIOR, 0) == 2);
                break;
            case KEY_SHOW_NAVBAR:
                int nav = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.SHOW_NAVBAR, -1, UserHandle.USER_CURRENT);
                mShowNavbarPreference.setChecked(nav == NAVBAR_SHOW);
                break;
            default: break;
        }
    }
    
    @Override
    public boolean onPreferenceClick(Preference preference) {
        return onPreferenceChange(preference, null);
    }
    
    private void applyNavbarSetting(boolean enable) {
        Settings.System.putIntForUser(getContentResolver(),
            Settings.System.SHOW_NAVBAR, enable ? 1 : 0,
            UserHandle.USER_CURRENT);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if(DEBUG) Log.d(TAG, "onPreferenceChange: " + preference.getKey() +
                    " : " + objValue);
        switch(preference.getKey()) {
            case KEY_LONG_PRESS_HOME_BUTTON_ASSIST:
                Settings.System.putInt(getContentResolver(),
                    Settings.System.LONG_PRESS_HOME_BUTTON_BEHAVIOR,
                        ((Boolean)objValue) ? 2 : 0);
                break;
            case KEY_SHOW_NAVBAR:
                applyNavbarSetting((boolean)objValue);
                mShowNavbarPreference.setEnabled(false);
                mHandler.postDelayed(resetNavbarToggle, 480);
                break;
            default: break;
        }
        updateState(preference.getKey());
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_buttons;
    }

}
