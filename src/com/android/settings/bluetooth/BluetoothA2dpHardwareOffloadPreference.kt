/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.icu.text.ListFormatter
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import com.android.bluetooth.audioquality.aidl.A2dpHardwareOffload
import com.android.settingslib.R
import com.android.settingslib.bluetooth.audioquality.AudioQualityServiceClient
import com.android.settingslib.spa.framework.util.collectLatestWithLifecycle
import kotlinx.coroutines.Job

/**
 * Lists the A2DP codecs that the phone encodes in hardware offload, as the audio quality service of
 * the Bluetooth app reports them for [device] while [fragment] is started. The row opens
 * [BluetoothA2dpHardwareOffloadDialogFragment] to choose them. The row is shown only while [device]
 * has an A2DP codec status and the phone can offload a codec, and is greyed out while the developer
 * option sets hardware offload off.
 */
internal class BluetoothA2dpHardwareOffloadPreference(
    context: Context,
    private val audioQualityClient: AudioQualityServiceClient,
    private val device: BluetoothDevice,
    private val fragment: Fragment,
) : Preference(context) {
    /** The Job that collects the quality for [device] while the row is attached. */
    private var qualityCollection: Job? = null

    init {
        key = KEY
        isVisible = false
        // the name of the row
        setTitle(R.string.bluetooth_a2dp_hw_offload_title)
    }

    private fun showHardwareOffload(offload: A2dpHardwareOffload?) {
        isVisible = offload != null && offload != A2dpHardwareOffload.Unsupported
        offload?.let {
            isEnabled = it is A2dpHardwareOffload.Enabled
            summary = it.formatSummary(context)
        }
    }

    override fun onClick() =
        BluetoothA2dpHardwareOffloadDialogFragment.show(device, fragment.childFragmentManager)

    override fun onAttached() {
        super.onAttached()
        qualityCollection =
            audioQualityClient.qualityFlow(device).collectLatestWithLifecycle(fragment) {
                showHardwareOffload(it?.hardwareOffload)
            }
    }

    override fun onDetached() {
        qualityCollection?.cancel()
        super.onDetached()
    }

    companion object {
        const val KEY = "bluetooth_a2dp_hw_offload"
    }
}

/** Formats the offloaded codecs, such as "aptX and aptX HD", or the text for software only. */
private fun A2dpHardwareOffload.formatSummary(context: Context): String {
    val offloadedCodecNames = codecs.filter { it.isOffloaded }.map { it.codecType.codecName }
    // the choice leaves every codec in software encoding
    return if (offloadedCodecNames.isEmpty()) {
        context.getString(R.string.bluetooth_a2dp_hw_offload_none)
    } else {
        ListFormatter.getInstance().format(offloadedCodecNames)
    }
}
