/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.audioquality.A2dpHardwareOffload
import android.bluetooth.audioquality.AudioQualityManager
import android.content.Context
import android.icu.text.ListFormatter
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.android.settingslib.R

/**
 * Lists the A2DP codecs encoded in hardware offload by the phone, as reported by
 * [AudioQualityManager] with the quality of [device], and opens
 * [BluetoothA2dpHardwareOffloadDialogFragment] to choose them. The row is shown only while [device]
 * has an A2DP codec status and the phone can offload a codec, and is greyed out while hardware
 * offload is off for every codec.
 */
internal class BluetoothA2dpHardwareOffloadPreference(
    context: Context,
    private val audioQualityManager: AudioQualityManager,
    private val device: BluetoothDevice,
    private val fragmentManager: FragmentManager,
) : Preference(context) {
    private val qualityCollector =
        AudioQualityManager.Collector { showHardwareOffload(it?.hardwareOffload) }

    init {
        key = KEY
        isVisible = false
        // the name of the row
        setTitle(R.string.bluetooth_a2dp_hw_offload_title)
    }

    private fun showHardwareOffload(offload: A2dpHardwareOffload?) {
        val choosable = offload?.takeIf { it.offloadableCodecTypes.isNotEmpty() }
        isVisible = choosable != null
        choosable?.let {
            isEnabled = !it.isDisabled
            summary = it.formatSummary(context)
        }
    }

    override fun onClick() =
        BluetoothA2dpHardwareOffloadDialogFragment.show(device, fragmentManager)

    override fun onAttached() {
        super.onAttached()
        audioQualityManager.collect(device, context.mainExecutor, qualityCollector)
    }

    override fun onDetached() {
        audioQualityManager.cancel(qualityCollector)
        super.onDetached()
    }

    companion object {
        const val KEY = "bluetooth_a2dp_hw_offload"
    }
}

/** Formats the offloaded codecs, such as "aptX and aptX HD", or the text for software only. */
private fun A2dpHardwareOffload.formatSummary(context: Context): String =
    // the choice leaves every codec in software encoding
    if (offloadedCodecTypes.isEmpty()) context.getString(R.string.bluetooth_a2dp_hw_offload_none)
    else ListFormatter.getInstance().format(offloadedCodecTypes.map { it.codecName })
