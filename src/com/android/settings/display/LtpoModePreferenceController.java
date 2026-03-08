/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 The halogenOS Project
 */

package com.android.settings.display;

import android.content.Context;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class LtpoModePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String AIDL_INSTANCE =
            "custom.hardware.display.ltpo.ILtpoControl/default";
    private static final String PROP_SFM_MODE = "persist.sys.sfm.mode";
    private static final float PEAK_REFRESH_RATE = 120f;

    private ListPreference mPreference;

    public LtpoModePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return ServiceManager.isDeclared(AIDL_INSTANCE) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        String mode = SystemProperties.get(PROP_SFM_MODE, "off");
        if (!"off".equals(mode) && !"idle".equals(mode) && !"vrr".equals(mode)) {
            mode = "off";
        }
        mPreference.setValue(mode);
        updateSummary(mode);
        mPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String mode = (String) newValue;
        boolean active = !"off".equals(mode);

        SystemProperties.set(PROP_SFM_MODE, mode);

        if (active) {
            // SFM requires 120Hz base — lock min and peak to 120Hz.
            Settings.System.putFloat(
                    mContext.getContentResolver(),
                    Settings.System.PEAK_REFRESH_RATE, PEAK_REFRESH_RATE);
            Settings.System.putFloat(
                    mContext.getContentResolver(),
                    Settings.System.MIN_REFRESH_RATE, PEAK_REFRESH_RATE);
        } else {
            Settings.System.putFloat(
                    mContext.getContentResolver(),
                    Settings.System.MIN_REFRESH_RATE, 0f);
        }

        updateSummary(mode);
        return true;
    }

    private void updateSummary(String mode) {
        switch (mode) {
            case "idle":
                mPreference.setSummary(R.string.ltpo_mode_summary_idle);
                break;
            case "vrr":
                mPreference.setSummary(R.string.ltpo_mode_summary_vrr);
                break;
            default:
                mPreference.setSummary(R.string.ltpo_mode_summary_off);
                break;
        }
    }
}
