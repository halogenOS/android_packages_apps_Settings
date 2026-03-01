// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.aboutphone

import android.app.settings.SettingsEnums
import android.content.Context
import androidx.fragment.app.Fragment
import com.android.settings.R
import com.android.settings.core.PreferenceScreenMixin
import com.android.settings.deviceinfo.hardwareinfo.HardwareInfoScreen
import com.android.settings.deviceinfo.imei.ImeiPreference
import com.android.settings.deviceinfo.imei.getImeiList
import com.android.settings.deviceinfo.simstatus.SimEidPreference
import com.android.settings.flags.Flags
import com.android.settings.wifi.utils.activeModemCount
import com.android.settingslib.metadata.PreferenceCategory
import com.android.settingslib.metadata.ProvidePreferenceScreen
import com.android.settingslib.metadata.preferenceHierarchy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// LINT.IfChange
/** The page behind the More card of About phone, with the rows that Catalyst binds on it. */
@ProvidePreferenceScreen(MyDeviceInfoMoreScreen.KEY)
class MyDeviceInfoMoreScreen : PreferenceScreenMixin {

    override val key: String
        get() = KEY

    override val purpose: Int
        get() = R.string.my_device_info_more_pref_screen_purpose

    override val title: Int
        get() = R.string.about_phone_more

    override val highlightMenuKey: Int
        get() = R.string.menu_key_about_device

    override fun getMetricsCategory() = SettingsEnums.DEVICEINFO

    override fun isFlagEnabled(context: Context) = Flags.catalystMyDeviceInfoPrefScreen()

    override fun fragmentClass(): Class<out Fragment>? = MyDeviceInfoMoreFragment::class.java

    override fun getPreferenceHierarchy(context: Context, coroutineScope: CoroutineScope) =
        preferenceHierarchy(context) {
            +PreferenceCategory(
                key = DEVICE_DETAIL_CATEGORY,
                purpose = R.string.device_detail_category_purpose,
                title = R.string.my_device_info_device_details_category_title,
            ) +=
                {
                    +HardwareInfoScreen.KEY order 30
                    addAsync(coroutineScope, Dispatchers.Default) {
                        +SimEidPreference(context) order 31
                    }
                    val activeModemCount = context.activeModemCount
                    val imeiList = context.getImeiList
                    for (i in 0 until activeModemCount) {
                        +ImeiPreference(context, i, activeModemCount, imeiList) order (i + 33)
                    }
                }
        }

    override fun hasCompleteHierarchy() = false

    companion object {
        const val KEY = "my_device_info_more_pref_screen"

        private const val DEVICE_DETAIL_CATEGORY = "device_detail_category"
    }
}
// LINT.ThenChange(/res/xml/my_device_info_more.xml)
