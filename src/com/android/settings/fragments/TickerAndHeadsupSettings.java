/*
 *  Copyright (C) 2017 The halogenOS Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.android.settings.fragments;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.preferences.SystemSettingSwitchPreference;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.settings.R;

public class TickerAndHeadsupSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String PREF_SHOW_TICKER = "status_bar_show_ticker";
    private static final String PREF_SHOW_HEADSUP = "enable_headsup";
    private static final String PREF_NOTIF_COUNTER = "status_bar_notif_count";
    private SystemSettingSwitchPreference mShowTicker;
    private SystemSettingSwitchPreference mHeadsup;
    private SystemSettingSwitchPreference mNotifCounter;


    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.XOS;
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.ticker_and_headsup);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mShowTicker = (SystemSettingSwitchPreference) prefSet.findPreference(PREF_SHOW_TICKER);
        if (mShowTicker != null){
          mShowTicker.setChecked(Settings.System.getInt(resolver,
                  Settings.System.STATUS_BAR_SHOW_TICKER, 0) != 0);
          mShowTicker.setOnPreferenceChangeListener(this);
        }

        mHeadsup = (SystemSettingSwitchPreference) findPreference(PREF_SHOW_HEADSUP);
        if (mHeadsup != null){
          mHeadsup.setChecked(Settings.System.getInt(resolver,
                  Settings.System.KEY_ENABLE_HEADSUP_NOTIFICATIONS, 0) != 0);
          mHeadsup.setOnPreferenceChangeListener(this);
        }

        mNotifCounter = (SystemSettingSwitchPreference) findPreference(PREF_NOTIF_COUNTER);
        if (mNotifCounter != null){
          mNotifCounter.setChecked(Settings.System.getInt(resolver,
                  Settings.System.STATUS_BAR_NOTIF_COUNT, 0) != 0);
          mNotifCounter.setOnPreferenceChangeListener(this);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mShowTicker) {
            if ((boolean) objValue) {
                mHeadsup.setChecked(false);
                Settings.System.putInt(resolver,
                    Settings.System.KEY_ENABLE_HEADSUP_NOTIFICATIONS,
                    ((boolean) objValue) ? 0 : 1);
            }
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER,
                ((boolean) objValue) ? 1 : 0);
        } else if (preference == mHeadsup) {
            if ((boolean) objValue) {
                mShowTicker.setChecked(false);
                Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER,
                    ((boolean) objValue) ? 0 : 1);
            }
            Settings.System.putInt(resolver,
                Settings.System.KEY_ENABLE_HEADSUP_NOTIFICATIONS,
                ((boolean) objValue) ? 1 : 0);

        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_ticker_headsup;
    }

}
