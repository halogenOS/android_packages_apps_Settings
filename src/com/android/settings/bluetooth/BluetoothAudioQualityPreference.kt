/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.audioquality.AudioQuality
import android.bluetooth.audioquality.AudioQualityManager
import android.content.Context
import androidx.preference.ListPreference
import com.android.settingslib.bluetooth.audioquality.formatNameWithBitrate
import com.android.settingslib.bluetooth.audioquality.formatSummaryWithBitrate
import com.android.settingslib.bluetooth.audioquality.isAdjustable
import com.android.settingslib.bluetooth.setLevelAndLogFailure

/**
 * Lists the audio quality levels of the A2DP codec used by [device], and sets the chosen level
 * through [AudioQualityManager]. The row is shown only when the stack reports levels for the codec
 * used by [device].
 */
internal class BluetoothAudioQualityPreference(
    context: Context,
    private val audioQualityManager: AudioQualityManager,
    private val device: BluetoothDevice,
) : ListPreference(context) {
    private val qualityCollector = AudioQualityManager.Collector(::showQuality)

    init {
        key = KEY
        isVisible = false
        setTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        setDialogTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        // The Bluetooth stack stores the level, and the row shows the level it reports.
        isPersistent = false
        setOnPreferenceChangeListener { _, chosenLevelId ->
            if (chosenLevelId is String) {
                audioQualityManager.setLevelAndLogFailure(device, chosenLevelId)
            }
            false
        }
    }

    private fun showQuality(quality: AudioQuality?) {
        val adjustable = quality?.takeIf { it.isAdjustable }
        isVisible = adjustable != null
        adjustable?.let {
            summary = it.formatSummaryWithBitrate(context)
            entries = it.levels.map { level -> level.formatNameWithBitrate(context) }.toTypedArray()
            entryValues = it.levels.map { level -> level.id }.toTypedArray()
            value = it.currentLevelId
        }
    }

    override fun onAttached() {
        super.onAttached()
        audioQualityManager.collect(device, context.mainExecutor, qualityCollector)
    }

    override fun onDetached() {
        audioQualityManager.cancel(qualityCollector)
        super.onDetached()
    }

    companion object {
        const val KEY = "bluetooth_audio_quality"
    }
}
