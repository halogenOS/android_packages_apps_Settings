/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.ListPreference
import com.android.settingslib.bluetooth.A2dpProfile
import com.android.settingslib.bluetooth.audioquality.HeadsetAudioQuality
import com.android.settingslib.bluetooth.audioquality.codecs.audioQualityCatalogue
import com.android.settingslib.bluetooth.audioquality.createAudioQualityChangeFilter
import com.android.settingslib.bluetooth.audioquality.formatSummaryWithBitrate
import com.android.settingslib.bluetooth.audioquality.formatTitleWithBitrate

/**
 * Lists the audio quality levels of the codec in use on the A2DP connection to [device], and sends
 * the chosen level to the Bluetooth stack. The row is hidden while the device has no A2DP
 * connection or its codec runs at a fixed quality.
 */
internal class BluetoothAudioQualityPreference(
    context: Context,
    private val a2dp: A2dpProfile,
    private val device: BluetoothDevice,
) : ListPreference(context) {
    private val qualityChangeReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = refresh()
        }

    init {
        key = KEY
        setTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        setDialogTitle(com.android.settingslib.R.string.bluetooth_audio_quality_title)
        // The Bluetooth stack stores the level, and the row shows the level it reports.
        isPersistent = false
        setOnPreferenceChangeListener { _, chosenKey ->
            readQuality()
                ?.levels
                ?.find { it.key == chosenKey }
                ?.let { audioQualityCatalogue.select(a2dp, device, it) }
            false
        }
    }

    /** Reads the quality from the Bluetooth stack again and shows it. */
    fun refresh() {
        val quality = readQuality()
        isVisible = quality != null
        quality?.let(::showLevels)
    }

    private fun readQuality() =
        audioQualityCatalogue.readQuality(a2dp, device) as? HeadsetAudioQuality.Adjustable

    private fun showLevels(quality: HeadsetAudioQuality.Adjustable) {
        summary = quality.formatSummaryWithBitrate(context)
        entries = quality.levels.map { it.formatTitleWithBitrate(context) }.toTypedArray()
        entryValues = quality.levels.map { it.key }.toTypedArray()
        value = quality.current?.key
    }

    override fun onAttached() {
        super.onAttached()
        context.registerReceiver(qualityChangeReceiver, createAudioQualityChangeFilter())
    }

    override fun onDetached() {
        context.unregisterReceiver(qualityChangeReceiver)
        super.onDetached()
    }

    companion object {
        const val KEY = "bluetooth_audio_quality"
    }
}
