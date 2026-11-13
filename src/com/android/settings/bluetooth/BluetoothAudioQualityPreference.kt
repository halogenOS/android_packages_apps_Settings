/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.audioquality.AudioQuality
import android.bluetooth.audioquality.AudioQualityLevel
import android.bluetooth.audioquality.AudioQualityManager
import android.content.Context
import androidx.preference.ListPreference
import com.android.settingslib.bluetooth.audioquality.currentLevelIndex
import com.android.settingslib.bluetooth.audioquality.formatLabel
import com.android.settingslib.bluetooth.audioquality.formatSummary
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

    /** The levels shown by the entries, in the same order. Each entry value is a position in it. */
    private var shownLevels: List<AudioQualityLevel> = emptyList()

    /** The levels of the entries copied by the dialog when it opened. */
    private var dialogLevels: List<AudioQualityLevel> = emptyList()

    init {
        key = KEY
        isVisible = false
        setTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        setDialogTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        // The Bluetooth stack stores the level, and the row shows the level it reports.
        isPersistent = false
        setOnPreferenceChangeListener { _, chosenIndex ->
            (chosenIndex as? String)
                ?.let { dialogLevels.getOrNull(it.toInt()) }
                ?.let { audioQualityManager.setLevelAndLogFailure(device, it) }
            false
        }
    }

    private fun showQuality(quality: AudioQuality?) {
        val adjustable = quality?.takeIf { it.isAdjustable }
        isVisible = adjustable != null
        shownLevels = adjustable?.levels.orEmpty()
        adjustable?.let {
            summary = it.formatSummary(context)
            entries = it.levels.map { level -> level.formatLabel(context) }.toTypedArray()
            entryValues = it.levels.indices.map(Int::toString).toTypedArray()
            value = it.currentLevelIndex?.toString()
        }
    }

    override fun onClick() {
        dialogLevels = shownLevels
        super.onClick()
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
