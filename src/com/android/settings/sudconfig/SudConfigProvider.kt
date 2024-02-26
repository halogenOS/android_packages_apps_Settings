package com.android.settings.sudconfig

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.android.settings.R;
import java.util.Optional

/**
 * Provides system-wide config for setup wizard screens.
 */
class SudConfigProvider : NonRelationalProvider() {
    companion object {
        private const val TAG = "SudConfigProvider"

        // resources to be forwarded via overlay config
        private val overlayConfigResources = arrayOf(
            R.dimen.setup_design_card_view_intrinsic_height,
            R.dimen.setup_design_card_view_intrinsic_width,
            R.bool.setup_compat_light_navigation_bar,
            R.bool.setup_compat_light_status_bar,
            R.color.setup_compat_footer_primary_button_bg_color,
            R.dimen.setup_compat_footer_button_radius,
            R.fraction.setup_compat_footer_button_ripple_alpha
        )
    }

    private lateinit var defaultThemeString: String

    override fun onCreate(): Boolean {
        // returns value of setupwizard.theme sysprop
        val theme: Optional<String> = android.sysprop.SetupWizardProperties.theme()
        // setupwizard.theme should always be set to prevent inconsistencies in setupdesign UIs
        check(theme.isPresent) { "missing setupwizard.theme sysprop" }
        defaultThemeString = theme.get()
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        Log.d(TAG, "method: $method, caller: $callingPackage")
        val bundle = Bundle()
        when (method) {
            "suwDefaultThemeString" -> bundle.putString(method, defaultThemeString)

            "applyGlifThemeControlledTransition",
            "isDynamicColorEnabled",
            "isEmbeddedActivityOnePaneEnabled",
            "isFullDynamicColorEnabled",
            "IsMaterialYouStyleEnabled",
            "isNeutralButtonStyleEnabled",
            "isSuwDayNightEnabled" -> bundle.putBoolean(method, true)

            "getDeviceName" -> bundle.putCharSequence(method, getDeviceName())

            "getOverlayConfig" -> fillOverlayConfig(bundle)
        }
        return bundle
    }

    private fun getDeviceName(): String {
        var name = Settings.Global.getString(
            requireContext().contentResolver,
            Settings.Global.DEVICE_NAME
        )
        if (TextUtils.isEmpty(name)) {
            name = Build.MODEL
        }
        return name
    }

    private fun fillOverlayConfig(bundle: Bundle) {
        overlayConfigResources.forEach { resId ->
            val context = requireContext()
            val resName = context.resources.getResourceEntryName(resId)
            val config = Bundle()
            config.putString("packageName", context.packageName)
            config.putString("resourceName", resName)
            config.putInt("resourceId", resId)
            bundle.putBundle(resName, config)
        }
    }
}
