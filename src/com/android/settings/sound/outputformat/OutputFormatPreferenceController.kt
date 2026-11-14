/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.outputformat

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The Output format row of Sound & vibration, summed up by how many devices switch. The summary
 * follows the devices while the row is shown.
 */
class OutputFormatPreferenceController(context: Context, key: String) :
    BasePreferenceController(context, key), DefaultLifecycleObserver {
    private var preference: Preference? = null
    private var summaryUpdates: Job? = null

    override fun getAvailabilityStatus() = AVAILABLE

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }

    override fun onStart(owner: LifecycleOwner) {
        summaryUpdates =
            owner.lifecycleScope.launch {
                mContext.labeledOutputDeviceUpdates(outputFormatManager()).collect { devices ->
                    val switching = devices.count { it.device.formatSwitchingEnabled }
                    preference?.summary =
                        mContext.resources.getQuantityString(
                            R.plurals.output_format_summary,
                            devices.size,
                            switching,
                            devices.size,
                        )
                }
            }
    }

    override fun onStop(owner: LifecycleOwner) {
        summaryUpdates?.cancel()
    }
}
