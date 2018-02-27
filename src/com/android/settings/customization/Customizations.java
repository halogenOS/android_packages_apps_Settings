/*
 * Copyright (C) 2018 The halogenOS Project
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

package com.android.settings.customization;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.preferences.CustomSeekBarPreference;
import com.android.settings.SettingsPreferenceFragment;

public class Customizations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = Customizations.class.getSimpleName();
    private static final String PREF_NAVBAR_TOGGLE_KEY = "navigation_bar_enabled";
    private static final String PREF_NAVBAR_TUNER_KEY = "tuner_navbar";
    private static final String PREF_KEY_BACKLIGHT_BRIGHTNESS = "button_backlight_brightness";
    private static final String PREF_KEY_BACKLIGHT_TIMEOUT = "button_backlight_timeout";

    private ContentResolver mResolver;

    private SwitchPreference mNavbarToggle;

    private Handler mHandler = new Handler();

    private final Runnable resetNavbarToggle = new Runnable() {
        @Override
        public void run() {
            mNavbarToggle.setEnabled(true);
        }
    };

    private CustomSeekBarPreference backlightBrightnessPreference;
    private CustomSeekBarPreference backlightTimeoutPreference;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.customizations);
        Context mContext = getContext();
        Preference prefSet = getPreferenceScreen();
        mResolver = getActivity().getContentResolver();

        mNavbarToggle = (SwitchPreference) findPreference(PREF_NAVBAR_TOGGLE_KEY);
        boolean mNavbarEnabled = Settings.Secure.getIntForUser(
            mResolver, Settings.Secure.NAVIGATION_BAR_ENABLED,
            getActivity().getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0,
            UserHandle.USER_CURRENT) == 1;
        mNavbarToggle.setChecked(mNavbarEnabled);
        findPreference(PREF_NAVBAR_TUNER_KEY).setEnabled(mNavbarEnabled);
        mNavbarToggle.setOnPreferenceChangeListener(this);

        backlightBrightnessPreference = (CustomSeekBarPreference)
            findPreference(PREF_KEY_BACKLIGHT_BRIGHTNESS);
        boolean hasVariableBrightness = mContext
                .getResources()
                .getBoolean(
                    com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);
        if (hasVariableBrightness) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            final int defaultButtonBrightness = pm.getDefaultButtonBrightness();
            final int currentBrightness = Settings.System.getInt(getContentResolver(),
                Settings.System.BUTTON_BRIGHTNESS, defaultButtonBrightness);
            backlightBrightnessPreference.setMax(pm.getMaximumScreenBrightnessSetting());
            backlightBrightnessPreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(PREF_KEY_BACKLIGHT_BRIGHTNESS);
        }
        backlightTimeoutPreference = (CustomSeekBarPreference) findPreference(PREF_KEY_BACKLIGHT_TIMEOUT);
        backlightTimeoutPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XOS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavbarToggle) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putIntForUser(mResolver,
                Settings.Secure.NAVIGATION_BAR_ENABLED, value ? 1 : 0,
                UserHandle.USER_CURRENT);
            mNavbarToggle.setChecked(value);
            mNavbarToggle.setEnabled(false);
            mHandler.postDelayed(resetNavbarToggle, 500);
            findPreference(PREF_NAVBAR_TUNER_KEY).setEnabled(value);
        } else if (preference == backlightBrightnessPreference) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.BUTTON_BRIGHTNESS, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == backlightTimeoutPreference) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.BUTTON_BACKLIGHT_TIMEOUT, val * 1000, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}
