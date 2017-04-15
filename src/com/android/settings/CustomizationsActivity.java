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

import com.plattysoft.leonids.ParticleSystem;
import android.view.animation.AccelerateInterpolator;
import java.util.Random;
import com.android.internal.util.omni.PackageUtils;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;


public class CustomizationsActivity extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        Indexable {
    private static final String TAG = CustomizationsActivity.class.getSimpleName();
    private static final String PREF_SHOW_TICKER = "status_bar_show_ticker";
    private static final String PREF_SHOW_HEADSUP = "enable_headsup";
    private static final String PREF_NOTIF_COUNTER = "status_bar_notif_count";
    private SwitchPreference mShowTicker;
    private SwitchPreference mHeadsup;
    private SwitchPreference mNotifCounter;


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
        if (mShowTicker != null){
          mShowTicker.setChecked(Settings.System.getInt(resolver,
                  Settings.System.STATUS_BAR_SHOW_TICKER, 0) != 0);
          mShowTicker.setOnPreferenceChangeListener(this);
        }

        mHeadsup = (SwitchPreference) findPreference(PREF_SHOW_HEADSUP);

        if (mHeadsup != null){
          mHeadsup.setChecked(Settings.System.getInt(resolver,
                  Settings.System.KEY_ENABLE_HEADSUP_NOTIFICATIONS, 0) != 0);
          mHeadsup.setOnPreferenceChangeListener(this);
        }

        mNotifCounter = (SwitchPreference) findPreference(PREF_NOTIF_COUNTER);
        if (mNotifCounter != null){
          mNotifCounter.setChecked(Settings.System.getInt(resolver,
                  Settings.System.STATUS_BAR_NOTIF_COUNT, 0) != 0);
          mNotifCounter.setOnPreferenceChangeListener(this);
        }
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
        if (PackageUtils.isImageTileInstalled(getContext())){
          Random rand =  new Random();
          int firstRandom = rand.nextInt(91 - 0);
          int secondRandom = rand.nextInt(181 - 90);
          int thirdRandom = rand.nextInt(181 - 0);
          Drawable flyingFucks = super.getResources().getDrawable(R.drawable.flying_fucks, null);
          ParticleSystem ps = new ParticleSystem(getActivity(), 100, flyingFucks, 3000);
          ps.setScaleRange(0.7f,1.3f);
          ps.setSpeedRange(0.1f, 0.25f);
          ps.setAcceleration(0.0001f, thirdRandom);
          ps.setRotationSpeedRange(firstRandom, secondRandom);
          ps.setFadeOut(200, new AccelerateInterpolator());
          ps.oneShot(this.getView(), 100);
        }
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mShowTicker) {
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER,
                    ((Boolean) objValue) ? 1 : 0);
            Settings.System.putInt(resolver,
                    Settings.System.KEY_ENABLE_HEADSUP_NOTIFICATIONS,
                    ((Boolean) objValue) ? 0 : 1);
            mHeadsup.setChecked(false);
        } else if (preference == mHeadsup) {
            Settings.System.putInt(resolver,
                Settings.System.KEY_ENABLE_HEADSUP_NOTIFICATIONS,
                (boolean)objValue ? 1 : 0);
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER,
                ((boolean) objValue) ? 0 : 1);
            mShowTicker.setChecked(false);
        } else if (preference == mNotifCounter){
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT,
                ((boolean) objValue) ? 1 : 0);
        }

        updateState(preference.getKey());
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_customizations;
    }

}
