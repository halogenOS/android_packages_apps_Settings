/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2017 The halogenOS Project
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

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.omni.PackageUtils;

import com.android.settings.search.Indexable;

public class DeviceSettingsActivity extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        Indexable {
    private static final String TAG = DeviceSettingsActivity.class.getSimpleName();

    private static final String KEY_GESTURES = "gestures";
    private static final String KEY_BUTTONS = "buttons";
    private static final String KEY_AMBIENT = "ambient_display";

    private static final String DOZE_PACKAGE = "org.halogenos.doze";
    private static final String ONEPLUS_SETTINGS_PACKAGE = "com.cyanogenmod.settings.device";

    private static final String GESTURES_ACTIVITY = ".TouchscreenGestureSettings";
    private static final String BUTTONS_ACTIVITY = ".ButtonSettings";
    private static final String AMBIENT_ACTIVITY = ".DozeSettings";

    private Preference mGesturesPreference;
    private Preference mButtonsPreference;
    private Preference mAmbientPreference;


    private void launchActivity(String packageName, String className) {
        Intent intent = new Intent();
        intent.setClassName(packageName, packageName + className);
        startActivity(intent, new Bundle());
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.XOS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.devicesettings);

        mGesturesPreference = findPreference(KEY_GESTURES);
        mButtonsPreference = findPreference(KEY_BUTTONS);
        mAmbientPreference = findPreference(KEY_AMBIENT);

        if (mAmbientPreference != null) {
            if (!PackageUtils.isAppInstalled(getContext(), DOZE_PACKAGE)) {
                removePreference(KEY_AMBIENT);
            } else {
                mAmbientPreference.setOnPreferenceClickListener(this);
            }
        }

        if (mGesturesPreference != null) {
            mGesturesPreference.setOnPreferenceClickListener(this);
        }

        if (mButtonsPreference != null) {
            mButtonsPreference.setOnPreferenceClickListener(this);
        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_GESTURES:
                launchActivity(ONEPLUS_SETTINGS_PACKAGE, GESTURES_ACTIVITY);
                break;
            case KEY_BUTTONS:
                launchActivity(ONEPLUS_SETTINGS_PACKAGE, BUTTONS_ACTIVITY);
                break;
            case KEY_AMBIENT:
                launchActivity(DOZE_PACKAGE, AMBIENT_ACTIVITY);
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_devicesettings;
    }

}
