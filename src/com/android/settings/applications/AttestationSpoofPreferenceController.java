/*
 * Copyright (C) 2026 The halogenOS Project
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

package com.android.settings.applications;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

/**
 * Toggle that gates the attestation / build-property spoof layer.
 * Disabled (greyed out) when {@code ro.boot.verifiedbootstate} reports
 * {@code green}, because an OEM-verified boot already produces an
 * attestation chain Play Integrity trusts.
 */
public class AttestationSpoofPreferenceController extends TogglePreferenceController {

    private static final String VERIFIED_BOOT_STATE = "ro.boot.verifiedbootstate";

    public AttestationSpoofPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return isGreen() ? DISABLED_DEPENDENT_SETTING : AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.ATTESTATION_SPOOF_ENABLED, 1) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.ATTESTATION_SPOOF_ENABLED, isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_apps;
    }

    private static boolean isGreen() {
        return "green".equals(SystemProperties.get(VERIFIED_BOOT_STATE, ""));
    }
}
