/*
 * Copyright (C) 2026 The halogenOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.spa.about

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.settings.R
import com.android.settingslib.audiostate.AudioStateRepository
import com.android.settingslib.audiostate.AudioStateSnapshot
import com.android.settings.bluetooth.Utils
import com.android.settingslib.audiostate.LocalBluetoothBatteryProvider
import com.android.settingslib.audiostate.compose.AudioStateTree
import com.android.settingslib.spa.framework.common.SettingsPageProvider
import com.android.settingslib.spa.framework.common.SpaEnvironmentFactory
import com.android.settingslib.spa.widget.scaffold.RegularScaffold

/**
 * Read-only "Audio information" page rendering the full system audio path via the shared
 * [AudioStateTree] composable — the same component used by the Quick Settings audio dialog, here
 * with the exhaustive device/microphone capability matrix enabled. Registered in
 * SettingsSpaEnvironment so it resolves both from About > More and from external deep-links
 * (destination name "AudioInformation").
 */
object AudioInformationPageProvider : SettingsPageProvider {
    override val name = "AudioInformation"

    @Composable
    override fun Page(arguments: Bundle?) {
        RegularScaffold(title = getTitle(arguments)) {
            val context = LocalContext.current
            // One repository per page instance, fed a main-looper handler for its framework
            // callbacks. The flow is lifecycle-scoped, so callbacks unregister when the page leaves.
            val repository = remember(context) {
                AudioStateRepository(
                    context.applicationContext,
                    Handler(Looper.getMainLooper()),
                    deviceBatteryProvider =
                        // Use Settings' shared LocalBluetoothManager, never a fresh getInstance —
                        // creating another would clobber the singleton other BT UI depends on.
                        LocalBluetoothBatteryProvider(Utils.getLocalBtManager(context)),
                )
            }
            val snapshot: AudioStateSnapshot? by
                repository.audioState.collectAsStateWithLifecycle(initialValue = null)

            snapshot?.let {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    AudioStateTree(snapshot = it, full = true)
                }
            }
        }
    }

    override fun getTitle(arguments: Bundle?): String =
        SpaEnvironmentFactory.instance.appContext.getString(R.string.audio_information_title)
}
