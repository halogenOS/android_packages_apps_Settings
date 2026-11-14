/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.bluetooth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.android.settingslib.bluetooth.audioquality.AudioQualityServiceClient

/**
 * The [AudioQualityServiceClient] of a device page, used by its rows and by the dialogs they open,
 * so the page binds the audio quality service of the Bluetooth app once.
 */
class AudioQualityClientViewModel(application: Application) : AndroidViewModel(application) {
    val client = AudioQualityServiceClient(application, viewModelScope)

    companion object {
        /** Returns the client of the device page that is [page]. */
        @JvmStatic
        fun clientOf(page: ViewModelStoreOwner): AudioQualityServiceClient =
            ViewModelProvider(page)[AudioQualityClientViewModel::class.java].client
    }
}
