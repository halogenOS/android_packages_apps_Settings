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

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.os.SystemProperties
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding
import java.nio.charset.StandardCharsets
import java.util.Base64

class DeviceMaintainerPreference :
    PreferenceMetadata,
    PreferenceAvailabilityProvider,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "device_maintainer"

    override val title: Int
        get() = R.string.device_maintainer

    override fun isAvailable(context: Context) = getDeviceMaintainer().isNotEmpty()

    override fun getSummary(context: Context): CharSequence? = getDeviceMaintainer()

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }

    companion object {
        private fun getDeviceMaintainer(): String {
            val maintainers = SystemProperties.get("ro.custom.build.device.maintainer", "")
            if (maintainers.isNotEmpty()) {
                return try {
                    String(Base64.getUrlDecoder().decode(maintainers), StandardCharsets.UTF_8)
                } catch (e: IllegalArgumentException) {
                    ""
                }
            }
            return ""
        }
    }
}
