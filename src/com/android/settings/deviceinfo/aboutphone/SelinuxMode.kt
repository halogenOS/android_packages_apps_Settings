// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.aboutphone

import android.os.SELinux
import androidx.annotation.StringRes
import com.android.settings.R

/** The modes SELinux runs in, with the label About phone shows for each. */
internal enum class SelinuxMode(@StringRes val label: Int) {
    DISABLED(R.string.selinux_status_disabled),
    PERMISSIVE(R.string.selinux_status_permissive),
    ENFORCING(R.string.selinux_status_enforcing);

    companion object {
        fun current(): SelinuxMode =
            when {
                !SELinux.isSELinuxEnabled() -> DISABLED
                !SELinux.isSELinuxEnforced() -> PERMISSIVE
                else -> ENFORCING
            }
    }
}
