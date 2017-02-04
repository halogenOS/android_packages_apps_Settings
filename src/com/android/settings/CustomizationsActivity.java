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

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;


public class CustomizationsActivity extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        Indexable {
    private static final String TAG = CustomizationsActivity.class.getSimpleName();



    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.XOS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.customizations);

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
        updateState(preference.getKey());
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_customizations;
    }

}
