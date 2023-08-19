/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DeviceMaintainerPreferenceController extends BasePreferenceController {

    private static final String TAG = "DeviceMaintainerCtrl";

    private final String mCurrentDeviceMaintainer;

    public DeviceMaintainerPreferenceController(Context context, String key) {
        super(context, key);
        mCurrentDeviceMaintainer = getDeviceMaintainer();
    }

    private static String getDeviceMaintainer() {
        return SystemProperties.get("ro.custom.build.device.maintainer").replace("\\n", "\n");
    }

    @Override
    public int getAvailabilityStatus() {
        return !TextUtils.isEmpty(mCurrentDeviceMaintainer)
                ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        return mCurrentDeviceMaintainer;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        return true;
    }
}
