/*
 * SPDX-FileCopyrightText: The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.spa.about

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.settings.R
import com.android.settings.bluetooth.Utils
import com.android.settingslib.audiostate.AudioStateRepository
import com.android.settingslib.audiostate.compose.AudioInformationPage
import com.android.settingslib.spa.framework.common.SettingsPageProvider
import com.android.settingslib.spa.framework.common.SpaEnvironmentFactory
import com.android.settingslib.spa.widget.scaffold.RegularScaffold
import kotlinx.coroutines.Dispatchers

/**
 * The live audio paths, devices and microphones. AndroidManifest.xml opens it by the action
 * com.android.settings.AUDIO_INFORMATION_SETTINGS under the destination name [name].
 */
object AudioInformationPageProvider : SettingsPageProvider {
    override val name = "AudioInformation"

    @Composable
    override fun Page(arguments: Bundle?) {
        RegularScaffold(title = getTitle(arguments)) {
            val context = LocalContext.current
            val repository =
                remember(context) {
                    AudioStateRepository(
                        context.applicationContext,
                        Handler(Looper.getMainLooper()),
                        // The shared manager; a new one would replace it for the Bluetooth UI.
                        Utils.getLocalBtManager(context),
                        Dispatchers.Default,
                    )
                }
            val snapshot by repository.audioState.collectAsStateWithLifecycle(initialValue = null)
            AudioInformationPage(snapshot)
        }
    }

    override fun getTitle(arguments: Bundle?): String =
        SpaEnvironmentFactory.instance.appContext.getString(R.string.audio_information_title)
}
