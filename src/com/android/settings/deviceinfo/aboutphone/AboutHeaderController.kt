// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.aboutphone

import android.content.Context
import android.content.Intent
import android.icu.text.NumberFormat
import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import com.android.settings.core.SubSettingLauncher
import com.android.settings.deviceinfo.firmwareversion.CustomRomBuildVersionPreference
import com.android.settings.deviceinfo.firmwareversion.CustomRomVersionPreference
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionDetailPreference
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionSettings
import com.android.settings.deviceinfo.firmwareversion.KernelVersionPreference
import com.android.settings.deviceinfo.firmwareversion.SecurityPatchLevelPreference
import com.android.settings.deviceinfo.firmwareversion.SimpleBuildNumberPreference
import com.android.settings.deviceinfo.firmwareversion.VendorSecurityPatchLevelPreference
import com.android.settings.deviceinfo.firmwareversion.deviceMaintainers
import com.android.settings.utils.hasActivityFor
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.getPreferenceSummary
import com.android.settingslib.metadata.getPreferenceTitle
import com.android.settingslib.widget.LayoutPreference
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fills the header of About phone: the logo, the device and the people who maintain it, the version
 * cards, the card of device details and the card that opens the remaining rows.
 */
class AboutHeaderController(context: Context, key: String) :
    BasePreferenceController(context, key) {

    private lateinit var uptime: TextView

    override fun getAvailabilityStatus() = AVAILABLE

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val header: LayoutPreference =
            checkNotNull(screen.findPreference(preferenceKey)) {
                "$preferenceKey is missing from my_device_info.xml"
            }
        val buildNumber: Preference =
            checkNotNull(screen.findPreference(MyDeviceInfoApiFirstScreen.BUILD_NUMBER_KEY)) {
                "${MyDeviceInfoApiFirstScreen.BUILD_NUMBER_KEY} is missing from my_device_info.xml"
            }

        header.findViewById<TextView>(R.id.device_name).text = Build.DEVICE
        header.findViewById<TextView>(R.id.maintainers).showMaintainers()
        header
            .findViewById<View>(R.id.android_version_card)
            .showVersion(FirmwareVersionDetailPreference())
        header.findViewById<View>(R.id.rom_version_card).showVersion(CustomRomVersionPreference())

        val infoCard: ViewGroup = header.findViewById(R.id.info_card)
        val securityPatch = SecurityPatchLevelPreference()
        if (securityPatch.isAvailable(mContext)) {
            infoCard.addRow(R.drawable.ic_about_security).apply {
                show(securityPatch)
                startOnClick(securityPatch.intent(mContext))
            }
        }
        val vendorSecurityPatch = VendorSecurityPatchLevelPreference()
        if (vendorSecurityPatch.isAvailable(mContext)) {
            infoCard.addRow(R.drawable.ic_about_vendor_security).show(vendorSecurityPatch)
        }
        infoCard.addRow(R.drawable.ic_about_kernel).show(KernelVersionPreference())
        infoCard.addRow(R.drawable.ic_about_build).show(CustomRomBuildVersionPreference())
        infoCard.addRow(R.drawable.ic_about_build_number).apply {
            show(SimpleBuildNumberPreference())
            setOnClickListener { buildNumber.performClick() }
        }
        infoCard
            .addRow(R.drawable.ic_about_selinux)
            .show(
                title = mContext.getText(R.string.selinux_status),
                summary = mContext.getText(SelinuxMode.current().label),
            )
        val uptimeRow = infoCard.addRow(R.drawable.ic_about_uptime)
        uptimeRow.show(title = mContext.getText(R.string.status_up_time), summary = formatUptime())
        uptime = uptimeRow.requireViewById(android.R.id.summary)

        header.findViewById<View>(R.id.more_card).setOnClickListener {
            open(MyDeviceInfoMoreFragment::class, R.string.about_phone_more)
        }
    }

    override fun onViewCreated(viewLifecycleOwner: LifecycleOwner) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    uptime.text = formatUptime()
                    delay(1.seconds)
                }
            }
        }
    }

    private fun View.showVersion(metadata: PreferenceMetadata) {
        show(metadata)
        setOnClickListener { open(FirmwareVersionSettings::class, R.string.firmware_version) }
    }

    private fun View.startOnClick(intent: Intent) = setOnClickListener {
        if (mContext.packageManager.hasActivityFor(intent)) mContext.startActivity(intent)
    }

    private fun TextView.showMaintainers() {
        val maintainers = mContext.deviceMaintainers
        isVisible = maintainers.isNotEmpty()
        text = mContext.getString(R.string.maintained_by, maintainers.joinToString(", "))
    }

    private fun formatUptime(): String {
        val uptime = Uptime.now()
        return mContext.getString(
            R.string.uptime_with_deep_sleep,
            DateUtils.formatElapsedTime(uptime.sinceBoot.inWholeSeconds),
            NumberFormat.getPercentInstance().format(uptime.deepSleepShare),
        )
    }

    private fun open(fragment: KClass<out Fragment>, @StringRes title: Int) =
        SubSettingLauncher(mContext)
            .setDestination(fragment.java.name)
            .setTitleRes(title)
            .setSourceMetricsCategory(metricsCategory)
            .launch()

    companion object {
        private fun View.show(title: CharSequence?, summary: CharSequence?) {
            requireViewById<TextView>(android.R.id.title).text = title
            requireViewById<TextView>(android.R.id.summary).text = summary
        }

        private fun View.show(metadata: PreferenceMetadata) =
            show(metadata.getPreferenceTitle(context), metadata.getPreferenceSummary(context))

        private fun ViewGroup.addRow(@DrawableRes icon: Int): View =
            LayoutInflater.from(context).inflate(R.layout.about_device_info_row, this, false).also {
                it.requireViewById<ImageView>(android.R.id.icon).setImageResource(icon)
                addView(it)
            }
    }
}
