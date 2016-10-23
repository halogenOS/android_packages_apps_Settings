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

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;


public class CustomizationsActivity extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        Indexable {
    private static final String TAG = CustomizationsActivity.class.getSimpleName();
    private static final String PREF_SHOW_TICKER = "status_bar_show_ticker";
    private SwitchPreference mShowTicker;


    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.XOS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.customizations);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mShowTicker = (SwitchPreference) prefSet.findPreference(PREF_SHOW_TICKER);
        mShowTicker.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0) != 0);
        mShowTicker.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {

    }

    private void updateState(String key) {
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return onPreferenceChange(preference, null);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mShowTicker) {
            int enabled = ((Boolean) objValue) ? 1 : 0;
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER, enabled);
            Settings.System.putInt(resolver,
                    Settings.System.KEY_ENABLE_HEADSUP_NOTIFICATIONS, ((Boolean) objValue) ? 0 : 1);
            return true;
        }
        updateState(preference.getKey());
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_customizations;
    }

}
