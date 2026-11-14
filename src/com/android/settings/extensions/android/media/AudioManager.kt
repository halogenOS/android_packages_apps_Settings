/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.extensions.android.media

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Emits once on collection, for the audio devices present then, and again after each audio device
 * connects or disconnects.
 */
internal fun AudioManager.deviceChanges(): Flow<Unit> =
    callbackFlow {
            fun sendChange() {
                // A change is not sent after the collector stopped, or while one change already
                // waits in the conflated buffer.
                val unusedSent = trySend(Unit)
            }
            val callback =
                object : AudioDeviceCallback() {
                    override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) =
                        sendChange()

                    override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) =
                        sendChange()
                }
            // Registering reports the devices present through onAudioDevicesAdded.
            registerAudioDeviceCallback(callback, null /*handler*/)
            awaitClose { unregisterAudioDeviceCallback(callback) }
        }
        .conflate()
