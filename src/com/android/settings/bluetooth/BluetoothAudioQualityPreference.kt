/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.preference.ListPreference
import com.android.bluetooth.audioquality.aidl.AudioQuality
import com.android.settingslib.bluetooth.audioquality.AudioQualityServiceClient
import com.android.settingslib.bluetooth.audioquality.currentLevelIndex
import com.android.settingslib.bluetooth.audioquality.formatLabel
import com.android.settingslib.bluetooth.audioquality.formatSummary
import com.android.settingslib.bluetooth.audioquality.isAdjustable
import com.android.settingslib.spa.framework.util.collectLatestWithLifecycle
import kotlinx.coroutines.Job

/**
 * Lists the audio quality levels of the A2DP codec used by [device], as reported by the audio
 * quality service of the Bluetooth app while [lifecycleOwner] is started, and sets the chosen level
 * through that service. The row is shown only when the stack reports levels for the codec used by
 * [device].
 */
internal class BluetoothAudioQualityPreference(
    context: Context,
    private val audioQualityClient: AudioQualityServiceClient,
    private val device: BluetoothDevice,
    private val lifecycleOwner: LifecycleOwner,
) : ListPreference(context) {
    /** The Job that collects the quality for [device] while the row is attached. */
    private var qualityCollection: Job? = null

    /**
     * The quality shown by the entries, with its levels in the same order. Each entry value is a
     * position in them.
     */
    private var shownQuality: AudioQuality? = null

    /** The quality the dialog copied from the shown quality when it opened. */
    private var dialogQuality: AudioQuality? = null

    init {
        key = KEY
        isVisible = false
        setTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        setDialogTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        // The Bluetooth stack stores the level, and the row shows the level it reports.
        isPersistent = false
        setOnPreferenceChangeListener { _, chosenIndex ->
            (chosenIndex as? String)
                ?.let { dialogQuality?.levels?.getOrNull(it.toInt()) }
                ?.let { audioQualityClient.setLevel(device, it) }
            false
        }
    }

    private fun showQuality(quality: AudioQuality?) {
        val adjustable = quality?.takeIf { it.isAdjustable }
        isVisible = adjustable != null
        shownQuality = adjustable
        adjustable?.let {
            summary = it.formatSummary(context)
            entries = it.levels.map { level -> level.formatLabel(context) }.toTypedArray()
            entryValues = it.levels.indices.map(Int::toString).toTypedArray()
            value = it.currentLevelIndex?.toString()
        }
    }

    override fun onClick() {
        dialogQuality = shownQuality
        super.onClick()
    }

    override fun onAttached() {
        super.onAttached()
        qualityCollection =
            audioQualityClient
                .qualityFlow(device)
                .collectLatestWithLifecycle(lifecycleOwner, action = ::showQuality)
    }

    override fun onDetached() {
        qualityCollection?.cancel()
        super.onDetached()
    }

    companion object {
        const val KEY = "bluetooth_audio_quality"
    }
}
