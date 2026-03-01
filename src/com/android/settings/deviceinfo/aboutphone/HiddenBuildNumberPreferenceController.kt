// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.aboutphone

import android.content.Context
import com.android.settings.deviceinfo.BuildNumberPreferenceController

/**
 * The build number row of About phone, hidden because the header shows the build number. The build
 * number row of the header passes its taps to this row, so they still count towards developer
 * options: [com.android.settings.dashboard.DashboardFragment.onPreferenceTreeClick] hands every
 * click to every controller, available or not.
 */
class HiddenBuildNumberPreferenceController(context: Context, key: String) :
    BuildNumberPreferenceController(context, key) {

    override fun getAvailabilityStatus() = CONDITIONALLY_UNAVAILABLE
}
