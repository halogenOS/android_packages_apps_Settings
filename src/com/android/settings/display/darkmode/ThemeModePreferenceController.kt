// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.display.darkmode

import android.content.Context
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.accessibility.extensions.isPowerSaveMode
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.spa.flow.broadcastReceiverFlow
import com.android.settingslib.thememode.OverlayManagerDimOverlay
import com.android.settingslib.thememode.ThemeMode
import com.android.settingslib.thememode.ThemeModeFailure
import com.android.settingslib.thememode.ThemeModeRepository
import com.android.settingslib.thememode.UiModeManagerNightMode
import com.android.settingslib.thememode.ui.messageId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ThemeModePreferenceController(context: Context, key: String) :
    BasePreferenceController(context, key) {

    private val nightMode = UiModeManagerNightMode(context)
    private val themeModeRepository =
        ThemeModeRepository(nightMode, OverlayManagerDimOverlay(context))
    private val themeModeFailure = MutableStateFlow<ThemeModeFailure?>(null)
    private lateinit var preference: ThemeModePreference

    override fun getAvailabilityStatus() = AVAILABLE

    override fun getSliceHighlightMenuRes() = R.string.menu_key_display

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)!!
        preference.onThemeModeClick = ::applyThemeMode
    }

    override fun onViewCreated(viewLifecycleOwner: LifecycleOwner) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { themeModeRepository.state.collect { preference.state = it } }
                launch {
                    combine(
                            isPowerSaveModeFlow(),
                            nightMode.isActiveFlow,
                            themeModeFailure,
                            ::Triple,
                        )
                        .collect { (isPowerSaveMode, isNightMode, themeModeFailure) ->
                            showStatus(isPowerSaveMode, isNightMode, themeModeFailure)
                        }
                }
            }
        }
    }

    private fun showStatus(
        isPowerSaveMode: Boolean,
        isNightMode: Boolean,
        themeModeFailure: ThemeModeFailure?,
    ) {
        preference.isEnabled = !isPowerSaveMode
        preference.summary =
            when {
                isPowerSaveMode -> AutoDarkTheme.getBatterySaverStatus(mContext, isNightMode)
                themeModeFailure != null -> mContext.getString(themeModeFailure.messageId)
                else -> null
            }
    }

    private fun isPowerSaveModeFlow(): Flow<Boolean> =
        mContext
            .broadcastReceiverFlow(IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
            .map { mContext.isPowerSaveMode() }
            .onStart { emit(mContext.isPowerSaveMode()) }

    private fun applyThemeMode(mode: ThemeMode) {
        val failure = themeModeRepository.apply(mode) as? ThemeModeFailure
        failure?.let { Log.w(TAG, "The system did not apply the theme mode $mode: $it") }
        themeModeFailure.value = failure
    }

    private companion object {
        const val TAG = "ThemeModePreferenceController"
    }
}
