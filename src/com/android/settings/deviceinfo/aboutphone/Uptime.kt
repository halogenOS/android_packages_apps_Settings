// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.deviceinfo.aboutphone

import android.os.SystemClock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** The time since the device started, and the part of it that the device spent in deep sleep. */
internal data class Uptime(val sinceBoot: Duration, val asleep: Duration) {
    init {
        require(asleep in Duration.ZERO..sinceBoot) { "asleep $asleep is outside 0..$sinceBoot" }
    }

    /** The share of [sinceBoot] spent in deep sleep, from 0 to 1. */
    val deepSleepShare: Double
        get() = asleep / sinceBoot

    companion object {
        fun now(): Uptime {
            // The awake clock is read first: the boot clock read after it can only be further
            // ahead, so the sleep time cannot come out negative.
            val awake = SystemClock.uptimeMillis().milliseconds
            val sinceBoot = SystemClock.elapsedRealtime().milliseconds
            return Uptime(sinceBoot = sinceBoot, asleep = sinceBoot - awake)
        }
    }
}
