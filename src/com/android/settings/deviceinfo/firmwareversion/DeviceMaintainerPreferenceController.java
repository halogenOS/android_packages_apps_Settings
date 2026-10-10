// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/** Shows the row of {@link DeviceMaintainerPreference} on the XML version of the screen. */
public class DeviceMaintainerPreferenceController extends BasePreferenceController {

    private final DeviceMaintainerPreference mMetadata = new DeviceMaintainerPreference();

    public DeviceMaintainerPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return mMetadata.isAvailable(mContext) ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setTitle(mMetadata.getTitle(mContext));
    }

    @Override
    public CharSequence getSummary() {
        return mMetadata.getSummary(mContext);
    }
}
