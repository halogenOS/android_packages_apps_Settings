/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.outputformat

import android.app.settings.SettingsEnums
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreferenceCompat
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment
import kotlinx.coroutines.launch

/**
 * Settings › Sound & vibration › Output format: whether each output device switches to the format
 * of a track that plays alone, with the devices available now apart from those used before. The
 * page follows the devices while it is shown.
 */
class OutputFormatSettings : DashboardFragment() {
    private val manager = outputFormatManager()

    override fun getPreferenceScreenResId() = R.xml.output_format_settings

    override fun getLogTag() = "OutputFormatSettings"

    // The metrics enum has no page ID for this page.
    override fun getMetricsCategory() = SettingsEnums.PAGE_UNKNOWN

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val availableNow = checkNotNull(findPreference<PreferenceCategory>(KEY_AVAILABLE_NOW))
        val usedBefore = checkNotNull(findPreference<PreferenceCategory>(KEY_USED_BEFORE))
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                requireContext().labeledOutputDeviceUpdates(manager).collect { devices ->
                    val (connected, disconnected) = devices.partition { it.device.connected }
                    availableNow.showDevices(connected)
                    usedBefore.showDevices(disconnected)
                }
            }
        }
    }

    /** Shows a toggle for each of [devices], and hides the category while it has none. */
    private fun PreferenceCategory.showDevices(devices: List<LabeledOutputDevice>) {
        removeAll()
        devices.forEach { addPreference(createToggle(it)) }
        isVisible = devices.isNotEmpty()
    }

    /**
     * A toggle that sets whether [labeled] switches format. It changes once the audio service has
     * stored the setting.
     */
    private fun PreferenceCategory.createToggle(labeled: LabeledOutputDevice) =
        SwitchPreferenceCompat(context).apply {
            title = labeled.device.name ?: context.getString(labeled.kind.unnamedTitle)
            setSummary(labeled.kind.label)
            setIcon(labeled.kind.icon)
            isPersistent = false
            isChecked = labeled.device.formatSwitchingEnabled
            setOnPreferenceChangeListener { _, enabled ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (
                        manager.trySetFormatSwitchingEnabled(labeled.device.id, enabled as Boolean)
                    ) {
                        isChecked = enabled
                    }
                }
                false
            }
        }

    private companion object {
        const val KEY_AVAILABLE_NOW = "output_format_available_now"
        const val KEY_USED_BEFORE = "output_format_used_before"
    }
}
