/*
 * Copyright (C) 2019 The LineageOS Project
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
import android.os.SystemProperties;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class CustomBuildTypePreferenceController  extends BasePreferenceController {

    private static final String TAG = "CustomBuildTypePreferenceController";
    private static final String CUSTOM_BUILD_TYPE = "ro.custom.branding.version";

    public CustomBuildTypePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        String buildType = SystemProperties.get(CUSTOM_BUILD_TYPE);
        switch (buildType) {
            case "Official":
                return mContext.getString(R.string.custom_official);
            default:
                return mContext.getString(R.string.custom_unofficial);
        }
    }
}

