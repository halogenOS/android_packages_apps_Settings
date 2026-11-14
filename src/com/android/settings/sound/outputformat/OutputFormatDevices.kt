/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.outputformat

import android.content.Context
import android.media.AudioManager
import android.media.IAudioService
import android.media.IOutputFormatManager
import android.media.OutputFormatDevice
import android.media.OutputFormatDeviceId
import android.os.ServiceManager
import android.os.ServiceSpecificException
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.android.settings.R
import com.android.settings.extensions.android.media.deviceChanges
import com.android.settingslib.R as SettingsLibR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val TAG = "OutputFormatDevices"

/** The kind of an output device, which sets its label and icon on the Output format page. */
internal enum class OutputKind(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    /** The title of a device that reported no name. */
    @StringRes val unnamedTitle: Int = label,
) {
    BLUETOOTH(R.string.output_format_bluetooth, SettingsLibR.drawable.ic_headphone),
    USB_DAC(R.string.output_format_usb_dac, R.drawable.ic_usb),
    BUILT_IN_SPEAKER(
        R.string.output_format_built_in,
        SettingsLibR.drawable.ic_smartphone,
        unnamedTitle = R.string.output_format_phone_speaker,
    ),
    WIRED_HEADPHONES(
        R.string.output_format_wired,
        SettingsLibR.drawable.ic_headphone,
        unnamedTitle = R.string.output_format_wired_headphones,
    ),
}

/** A device of [IOutputFormatManager] with the kind shown for it on the Output format page. */
internal data class LabeledOutputDevice(val device: OutputFormatDevice, val kind: OutputKind)

/** The manager through which the audio service stores whether each device switches format. */
internal fun outputFormatManager(): IOutputFormatManager =
    IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE))
        .outputFormatManager

/**
 * Emits the connected devices and the devices connected before, in the order first seen by the
 * audio service, once on collection and again after each audio device connects or disconnects. The
 * list is read off the main thread. A device of a kind without a label on this page is left out.
 */
internal fun Context.labeledOutputDeviceUpdates(
    manager: IOutputFormatManager
): Flow<List<LabeledOutputDevice>> =
    getSystemService(AudioManager::class.java)
        .deviceChanges()
        .map { manager.listLabeledDevices() }
        .flowOn(Dispatchers.IO)

/**
 * Sets whether the device with [id] switches format, off the main thread, and returns whether the
 * audio service stored the setting.
 */
internal suspend fun IOutputFormatManager.trySetFormatSwitchingEnabled(
    id: OutputFormatDeviceId,
    enabled: Boolean,
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            setFormatSwitchingEnabled(id, enabled)
            true
        } catch (e: ServiceSpecificException) {
            Log.e(TAG, "The audio service did not store whether $id switches format", e)
            false
        }
    }

private fun IOutputFormatManager.listLabeledDevices(): List<LabeledOutputDevice> =
    devices.mapNotNull { device ->
        device.id.kind?.let { kind -> LabeledOutputDevice(device = device, kind = kind) }
    }

private val OutputFormatDeviceId.kind: OutputKind?
    get() =
        when (tag) {
            OutputFormatDeviceId.bluetoothAddress -> OutputKind.BLUETOOTH
            OutputFormatDeviceId.usbIds -> OutputKind.USB_DAC
            OutputFormatDeviceId.builtInOutput ->
                when (builtInOutput) {
                    OutputFormatDeviceId.BuiltInOutput.SPEAKER -> OutputKind.BUILT_IN_SPEAKER
                    OutputFormatDeviceId.BuiltInOutput.WIRED_HEADPHONES ->
                        OutputKind.WIRED_HEADPHONES
                    else -> null
                }
            else -> null
        }
