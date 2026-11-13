/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.app.Dialog
import android.bluetooth.BluetoothCodecType
import android.bluetooth.BluetoothDevice
import android.bluetooth.audioquality.AudioQuality
import android.bluetooth.audioquality.AudioQualityManager
import android.os.Bundle
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.settings.R
import com.android.settingslib.bluetooth.qualityFlow
import com.android.settingslib.bluetooth.setCodecOffloadedAndLogFailure
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private const val TAG = "A2dpHardwareOffloadDialog"
private const val ARG_DEVICE = "device"

/**
 * Chooses which A2DP codecs are encoded in hardware offload by the phone, for every headset. A tap
 * on a checkbox sends the change to [AudioQualityManager] at once, and the dialog shows the choice
 * reported by the Bluetooth stack with the quality of the device passed when the dialog opened.
 */
internal class BluetoothA2dpHardwareOffloadDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val audioQualityManager =
            checkNotNull(context.getSystemService(AudioQualityManager::class.java))
        val device =
            checkNotNull(requireArguments().getParcelable(ARG_DEVICE, BluetoothDevice::class.java))
        val content =
            LayoutInflater.from(context).inflate(R.layout.bluetooth_a2dp_hw_offload_dialog, null)
        val codecCheckBoxes =
            CodecCheckBoxes(
                content.requireViewById(R.id.bluetooth_a2dp_hw_offload_codecs),
                audioQualityManager,
            )
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioQualityManager
                    .qualityFlow(device)
                    .filterNotNull()
                    .collect(codecCheckBoxes::show)
            }
        }
        return AlertDialog.Builder(context)
            // the name of the row that opens this dialog
            .setTitle(com.android.settingslib.R.string.bluetooth_a2dp_hw_offload_title)
            .setView(content)
            // closes the dialog, every change has already been sent
            .setPositiveButton(com.android.settingslib.R.string.done, null)
            .create()
    }

    companion object {
        /** Opens the dialog with the choice reported with the quality of [device]. */
        fun show(device: BluetoothDevice, fragmentManager: FragmentManager) =
            BluetoothA2dpHardwareOffloadDialogFragment()
                .apply { arguments = bundleOf(ARG_DEVICE to device) }
                .show(fragmentManager, TAG)
    }
}

/**
 * One checkbox per codec offloadable by the phone, in [codecList]. [show] updates the checkboxes in
 * place, so a tap does not move the focus.
 */
private class CodecCheckBoxes(
    private val codecList: ViewGroup,
    private val audioQualityManager: AudioQualityManager,
) {
    private var checkBoxes: Map<BluetoothCodecType, CheckBox> = emptyMap()

    fun show(quality: AudioQuality) {
        val offload = quality.hardwareOffload
        if (checkBoxes.keys.toList() != offload.offloadableCodecTypes) {
            codecList.removeAllViews()
            checkBoxes = offload.offloadableCodecTypes.associateWith(::addCheckBox)
        }
        checkBoxes.forEach { (codecType, checkBox) ->
            checkBox.isChecked = codecType in offload.offloadedCodecTypes
            checkBox.text = formatCodecLabel(codecType, inUse = codecType == quality.codecType)
        }
    }

    private fun addCheckBox(codecType: BluetoothCodecType): CheckBox {
        val checkBox =
            LayoutInflater.from(codecList.context)
                .inflate(R.layout.bluetooth_a2dp_hw_offload_codec, codecList, false) as CheckBox
        // A click listener runs only on taps. A checked-change listener also runs when show() sets
        // the state. The tap is undone, so the checkbox shows the choice reported by the stack.
        checkBox.setOnClickListener {
            val offloaded = checkBox.isChecked
            checkBox.isChecked = !offloaded
            audioQualityManager.setCodecOffloadedAndLogFailure(codecType, offloaded)
        }
        codecList.addView(checkBox)
        return checkBox
    }

    /** The codec name, with a smaller line under it for the codec used by the headset. */
    private fun formatCodecLabel(codecType: BluetoothCodecType, inUse: Boolean): CharSequence =
        buildSpannedString {
            append(codecType.codecName)
            if (inUse) {
                appendLine()
                val context = codecList.context
                inSpans(
                    TextAppearanceSpan(context, android.R.style.TextAppearance_DeviceDefault_Small)
                ) {
                    // marks the codec that streams to the headset
                    append(
                        context.getString(
                            com.android.settingslib.R.string.bluetooth_a2dp_hw_offload_in_use
                        )
                    )
                }
            }
        }
}
