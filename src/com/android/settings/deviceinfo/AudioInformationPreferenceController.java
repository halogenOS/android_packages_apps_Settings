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
package com.android.settings.deviceinfo;

import android.content.Context;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.spa.SpaActivity;

/**
 * Opens the read-only "Audio information" SPA page, which renders the live system audio path
 * (route, devices and capabilities, microphones, codec, battery). The same page is the deep-link
 * target of the Quick Settings audio tile's long-press.
 */
public class AudioInformationPreferenceController extends BasePreferenceController {

    private static final String AUDIO_INFORMATION_DESTINATION = "AudioInformation";

    public AudioInformationPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), mPreferenceKey)) {
            SpaActivity.startSpaActivity(mContext, AUDIO_INFORMATION_DESTINATION);
            return true;
        }
        return false;
    }
}
