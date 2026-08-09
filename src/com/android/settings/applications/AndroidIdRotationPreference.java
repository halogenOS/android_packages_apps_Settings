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

package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.preference.Preference;

import com.android.settings.R;

import java.security.SecureRandom;

/**
 * Dev control that rotates the Google-visible device identity. GMS presents
 * the value of {@link Settings.Secure#ATTESTATION_ANDROID_ID_OVERRIDE} as its
 * checkin androidId (gservices "android_id") instead of the stored one; the
 * SSAID-derived synthetic serial and IMEI shift with it. The stored checkin
 * ID is never touched, so removing the override restores the original
 * identity exactly. Play services and Play Store are killed after each
 * change so they re-read on next start.
 */
public class AndroidIdRotationPreference extends Preference {

    private static final String GMS_PACKAGE = "com.google.android.gms";
    private static final String VENDING_PACKAGE = "com.android.vending";

    public AndroidIdRotationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {
        final Context ctx = getContext();
        final String override = getOverride();
        final boolean active = !TextUtils.isEmpty(override);

        // The stored checkin ID is unreadable: GMS's gservices provider
        // blocks device-ID access for non-Google packages, so only the
        // override (which we own) can be displayed.
        String gmsIdDisplay;
        if (active) {
            gmsIdDisplay = toHex(override)
                    + " " + ctx.getString(R.string.android_id_rotation_override_active);
        } else {
            gmsIdDisplay = ctx.getString(R.string.android_id_rotation_stored_hidden);
        }

        String osId = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(osId)) {
            osId = ctx.getString(R.string.android_id_rotation_unknown);
        }

        // GMS's per-app SSAID is resolvable only to GMS itself (the ssaid
        // table is not exposed to cross-package readers by design).
        String ssaid = ctx.getString(R.string.android_id_rotation_ssaid_unreadable);

        return ctx.getString(R.string.android_id_rotation_gms_id, gmsIdDisplay) + "\n"
                + ctx.getString(R.string.android_id_rotation_os_id, osId) + "\n"
                + ctx.getString(R.string.android_id_rotation_ssaid, ssaid);
    }

    @Override
    protected void onClick() {
        super.onClick();

        final boolean active = !TextUtils.isEmpty(getOverride());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(getTitle())
                .setMessage(R.string.android_id_rotation_dialog_message)
                .setPositiveButton(R.string.android_id_rotation_rotate, (dialog, which) -> rotate())
                .setNegativeButton(android.R.string.cancel, null);
        if (active) {
            builder.setNeutralButton(R.string.android_id_rotation_reset,
                    (dialog, which) -> resetOverride());
        }
        builder.show();
    }

    private void rotate() {
        final SecureRandom random = new SecureRandom();
        long newId;
        do {
            // positive 63-bit, non-zero — same domain as server-issued IDs
            newId = random.nextLong() & Long.MAX_VALUE;
        } while (newId == 0);

        Settings.Secure.putString(getContext().getContentResolver(),
                Settings.Secure.ATTESTATION_ANDROID_ID_OVERRIDE, Long.toString(newId));
        restartGoogleServices();
        notifyChanged();
    }

    private void resetOverride() {
        Settings.Secure.putString(getContext().getContentResolver(),
                Settings.Secure.ATTESTATION_ANDROID_ID_OVERRIDE, null);
        restartGoogleServices();
        notifyChanged();
    }

    private void restartGoogleServices() {
        final ActivityManager am = getContext().getSystemService(ActivityManager.class);
        am.killBackgroundProcesses(GMS_PACKAGE);
        am.killBackgroundProcesses(VENDING_PACKAGE);
    }

    private String getOverride() {
        return Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ATTESTATION_ANDROID_ID_OVERRIDE);
    }

    private static String toHex(String decimal) {
        try {
            return Long.toHexString(Long.parseLong(decimal));
        } catch (NumberFormatException e) {
            return decimal;
        }
    }
}
