// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.metadata.PreferenceTitleProvider
import com.android.settingslib.metadata.preferencesapi.preconditions.PreconditionStability
import com.android.settingslib.preference.PreferenceBinding

class DeviceMaintainerPreference :
    PreferenceMetadata,
    PreferenceAvailabilityProvider,
    PreferenceTitleProvider,
    PreferenceSummaryProvider,
    PreferenceBinding {

    override val key: String
        get() = "device_maintainer"

    override val purpose: Int
        get() = R.string.device_maintainer_purpose

    override val availabilityDescription =
        "The device must set a maintainer in its Settings overlay."

    override fun getAvailabilityStability() = PreconditionStability.STABLE_UNTIL_APK_UPDATE

    override fun isAvailable(context: Context) = context.deviceMaintainers.isNotEmpty()

    override fun getTitle(context: Context): CharSequence? =
        context.resources.getQuantityString(
            R.plurals.device_info_device_maintainers_title,
            context.deviceMaintainers.size,
        )

    override fun getSummary(context: Context): CharSequence? =
        context.deviceMaintainers.joinToString("\n")

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
    }
}

/** The names of the people who maintain the build for this device, empty when none are set. */
internal val Context.deviceMaintainers: List<String>
    get() =
        resources.getStringArray(R.array.device_info_device_maintainers).filter { it.isNotBlank() }
