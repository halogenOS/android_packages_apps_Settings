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
import android.text.format.DateFormat
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding
import java.text.SimpleDateFormat
import java.util.Locale

class VendorSecurityPatchLevelPreference :
    PreferenceMetadata,
    PreferenceAvailabilityProvider,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "vendor_security_key"

    override val title: Int
        get() = R.string.vendor_security_patch

    override fun isAvailable(context: Context) = getVendorSecurityPatch().isNotEmpty()

    override fun getSummary(context: Context): CharSequence? = getVendorSecurityPatch()

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }

    companion object {
        private fun getVendorSecurityPatch(): String {
            val patch = SystemProperties.get("ro.vendor.build.security_patch", "")
            if (patch.isNotEmpty()) {
                return try {
                    val format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy")
                    DateFormat.format(format, SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(patch)).toString()
                } catch (e: Exception) {
                    patch
                }
            }
            return ""
        }
    }
}
