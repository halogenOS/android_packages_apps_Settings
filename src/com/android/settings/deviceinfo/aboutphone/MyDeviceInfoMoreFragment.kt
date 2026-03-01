// SPDX-FileCopyrightText: The Android Open Source Project
// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.aboutphone

import android.app.settings.SettingsEnums
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment
import com.android.settings.deviceinfo.BluetoothAddressPreferenceController
import com.android.settings.deviceinfo.FccEquipmentIdPreferenceController
import com.android.settings.deviceinfo.FeedbackPreferenceController
import com.android.settings.deviceinfo.IpAddressPreferenceController
import com.android.settings.deviceinfo.ManualPreferenceController
import com.android.settings.deviceinfo.RegulatoryInfoPreferenceController
import com.android.settings.deviceinfo.SafetyInfoPreferenceController
import com.android.settings.deviceinfo.WifiMacAddressPreferenceController
import com.android.settings.deviceinfo.imei.ImeiInfoPreferenceController
import com.android.settings.deviceinfo.simstatus.EidStatus
import com.android.settings.deviceinfo.simstatus.SimEidPreferenceController
import com.android.settings.deviceinfo.simstatus.SimStatusPreferenceController
import com.android.settings.deviceinfo.simstatus.SlotSimStatus
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle
import com.android.settingslib.search.SearchIndexable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/** The device details of About phone not shown by its header. */
@SearchIndexable
class MyDeviceInfoMoreFragment : DashboardFragment() {

    override fun getMetricsCategory() = SettingsEnums.DEVICEINFO

    override fun getHelpResource() = R.string.help_uri_about

    override fun getLogTag() = TAG

    override fun getPreferenceScreenResId() = R.xml.my_device_info_more

    override fun getPreferenceScreenBindingKey(context: Context): String? =
        MyDeviceInfoMoreScreen.KEY

    override fun getPreferenceKeysInHierarchy(): MutableSet<String> =
        // Catalyst adds the EID row to the hierarchy asynchronously, so its key is not listed yet.
        super.getPreferenceKeysInHierarchy().apply { add(KEY_EID_INFO) }

    override fun onPreferenceScreenCreatedFromResource(preferenceScreen: PreferenceScreen) {
        // remove the preference created from resource to avoid duplicated key
        if (isCatalystEnabled) preferenceScreen.removePreferenceRecursively(KEY_EID_INFO)
    }

    override fun createPreferenceControllers(context: Context): List<AbstractPreferenceController> {
        val executor = Executors.newSingleThreadExecutor()
        try {
            val slotSimStatus = SlotSimStatus(context, executor, lifecycle)
            val controllers =
                buildSharedControllers(context, this, settingsLifecycle, slotSimStatus)
            // Catalyst binds the IMEI and EID rows itself.
            if (isCatalystEnabled) return controllers
            return controllers +
                buildImeiControllers(context, slotSimStatus) +
                buildEidController(context, slotSimStatus, executor)
        } finally {
            executor.shutdown()
        }
    }

    private fun buildImeiControllers(
        context: Context,
        slotSimStatus: SlotSimStatus,
    ): List<ImeiInfoPreferenceController> {
        val defaultKey = ImeiInfoPreferenceController.DEFAULT_KEY
        val slotKeys = List(slotSimStatus.size()) { defaultKey + (it + 1) }
        return (listOf(defaultKey) + slotKeys).map { key ->
            ImeiInfoPreferenceController(context, key).apply {
                init(this@MyDeviceInfoMoreFragment, slotSimStatus)
            }
        }
    }

    companion object {
        private const val TAG = "MyDeviceInfoMoreFragment"
        private const val KEY_EID_INFO = "eid_info"

        /** Builds the controllers of the rows that the page and settings search handle alike. */
        private fun buildSharedControllers(
            context: Context,
            fragment: Fragment?,
            lifecycle: Lifecycle?,
            slotSimStatus: SlotSimStatus,
        ): List<AbstractPreferenceController> = buildList {
            add(IpAddressPreferenceController(context, lifecycle))
            add(WifiMacAddressPreferenceController(context, lifecycle))
            add(BluetoothAddressPreferenceController(context, lifecycle))
            add(RegulatoryInfoPreferenceController(context))
            add(SafetyInfoPreferenceController(context))
            add(ManualPreferenceController(context))
            add(FeedbackPreferenceController(fragment, context))
            add(FccEquipmentIdPreferenceController(context))
            for (slotIndex in 0 until slotSimStatus.size()) {
                add(
                    SimStatusPreferenceController(
                            context,
                            slotSimStatus.getPreferenceKey(slotIndex),
                        )
                        .apply { init(fragment, slotSimStatus) }
                )
            }
        }

        private fun buildEidController(
            context: Context,
            slotSimStatus: SlotSimStatus,
            executor: Executor,
        ) =
            SimEidPreferenceController(context, KEY_EID_INFO).apply {
                init(slotSimStatus, EidStatus(slotSimStatus, context, executor))
            }

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER =
            object : BaseSearchIndexProvider(R.xml.my_device_info_more) {
                override fun createPreferenceControllers(
                    context: Context
                ): List<AbstractPreferenceController> {
                    val slotSimStatus = SlotSimStatus(context, context.mainExecutor, null)
                    return buildSharedControllers(
                        context,
                        fragment = null,
                        lifecycle = null,
                        slotSimStatus,
                    ) + buildEidController(context, slotSimStatus, context.mainExecutor)
                }
            }
    }
}
