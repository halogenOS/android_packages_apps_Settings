/*
 * Copyright (C) 2026 The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.display;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.android.settings.core.BasePreferenceController;

public class StandByWatchPreferenceController extends BasePreferenceController {

    private static final String PACKAGE_NAME = "custom.standbywatch";

    /**
     * One-way capability flag written by the watch service the first time it
     * observes wireless charging. There is no static framework signal for
     * wireless charging support, so the device learns it at runtime.
     */
    private static final String GLOBAL_WIRELESS_CHARGING_SUPPORTED =
            "wireless_charging_supported";

    public StandByWatchPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        try {
            mContext.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return UNSUPPORTED_ON_DEVICE;
        }
        final boolean wirelessChargingSeen = Settings.Global.getInt(
                mContext.getContentResolver(), GLOBAL_WIRELESS_CHARGING_SUPPORTED, 0) == 1;
        return wirelessChargingSeen ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }
}
