/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2024 The halogenOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.provider.Settings;
import android.view.Display;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class ForceAllowHBMPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_FORCE_ALLOW_HBM = "force_allow_hbm";
    private final Context context;

    public ForceAllowHBMPreferenceController(Context context) {
        super(context);
	this.context = context;
    }

    @Override
    public boolean isAvailable() {
        DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        for (Display display : displays) {
            if (display.isHdr()) {
                return true;
            }
        }
	return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_FORCE_ALLOW_HBM;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value = (Boolean) newValue;
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.FORCE_ALLOW_HBM, value ? 1 : 0);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.FORCE_ALLOW_HBM, 0);
        ((SwitchPreference) preference).setChecked(value != 0);
    }
}
