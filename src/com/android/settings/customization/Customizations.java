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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Customizations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = Customizations.class.getSimpleName();
    private static final String PREF_NAVBAR_TOGGLE_KEY = "navigation_bar_enabled";
    private static final String PREF_NAVBAR_TUNER_KEY = "tuner_navbar";

    private ContentResolver mResolver;

    private SwitchPreference mNavbarToggle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.customizations);
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
            findPreference(PREF_NAVBAR_TUNER_KEY).setEnabled(value);
            return true;
        }
        return false;
    }
}