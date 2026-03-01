/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.deviceinfo.aboutphone;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.SELinux;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.deviceinfo.BuildNumberPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.LayoutPreference;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@SearchIndexable
public class MyDeviceInfoFragment extends DashboardFragment {

    private static final String LOG_TAG = "MyDeviceInfoFragment";
    private static final String KEY_HALOGENOS_ABOUT_HEADER = "halogenos_about_header";
    private static final Uri SECURITY_BULLETIN_URI = Uri.parse(
            "https://source.android.com/docs/security/bulletin/");

    private BuildNumberPreferenceController mBuildNumberPreferenceController;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private TextView mUptimeView;

    private final Runnable mUptimeUpdater = new Runnable() {
        @Override
        public void run() {
            if (mUptimeView != null) {
                long elapsedMs = SystemClock.elapsedRealtime();
                long uptimeMs = SystemClock.uptimeMillis();
                String uptime = DateUtils.formatElapsedTime(elapsedMs / 1000);
                int deepSleepPct = elapsedMs > 0
                        ? (int) ((elapsedMs - uptimeMs) * 100 / elapsedMs) : 0;
                mUptimeView.setText(uptime + " (" + getString(R.string.deep_sleep)
                        + ": " + deepSleepPct + "%)");
            }
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DEVICEINFO;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_uri_about;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBuildNumberPreferenceController = use(BuildNumberPreferenceController.class);
        mBuildNumberPreferenceController.setHost(this /* parent */);
    }

    @Override
    public void onStart() {
        super.onStart();
        Preference buildPref = findPreference("build_number");
        if (buildPref != null) {
            buildPref.setVisible(false);
        }
        Preference morePref = findPreference("about_phone_more");
        if (morePref != null) {
            morePref.setVisible(false);
        }
        initAboutHeader();
        mHandler.post(mUptimeUpdater);
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mUptimeUpdater);
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.my_device_info;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        return new ArrayList<>();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBuildNumberPreferenceController.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initAboutHeader() {
        final LayoutPreference headerPref =
                getPreferenceScreen().findPreference(KEY_HALOGENOS_ABOUT_HEADER);
        if (headerPref == null) {
            return;
        }

        // Device name
        setText(headerPref, R.id.header_device_name, Build.MODEL);

        // Maintainer (conditionally visible)
        String maintainer = getMaintainer();
        if (!TextUtils.isEmpty(maintainer)) {
            TextView maintainerView = headerPref.findViewById(R.id.header_maintainer);
            if (maintainerView != null) {
                maintainerView.setText(getString(R.string.maintained_by, maintainer));
                maintainerView.setVisibility(View.VISIBLE);
            }
        }

        // Android version
        setText(headerPref, R.id.header_android_version,
                Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY);

        // XOS version
        setText(headerPref, R.id.header_xos_version,
                SystemProperties.get("ro.custom.display.version", Build.DISPLAY));

        // Security patch (clickable → opens security bulletin)
        String securityPatch = DeviceInfoUtils.getSecurityPatch();
        if (!TextUtils.isEmpty(securityPatch)) {
            setText(headerPref, R.id.header_security_patch, securityPatch);
            View securityRow = headerPref.findViewById(R.id.card_security_patch);
            if (securityRow != null) {
                securityRow.setOnClickListener(v -> openSecurityBulletin());
            }
        }

        // Vendor security patch (conditionally visible)
        String vendorPatch = getVendorSecurityPatch();
        if (!TextUtils.isEmpty(vendorPatch)) {
            setVisible(headerPref, R.id.row_vendor_security_patch);
            setVisible(headerPref, R.id.divider_after_vendor);
            setText(headerPref, R.id.header_vendor_security_patch, vendorPatch);
        }

        // Kernel version
        setText(headerPref, R.id.header_kernel_version,
                DeviceInfoUtils.getFormattedKernelVersion(getContext()));

        // Build version
        setText(headerPref, R.id.header_build_version,
                SystemProperties.get("ro.custom.version", ""));

        // Build number (clickable → dev mode tap counter)
        setText(headerPref, R.id.header_build_number, Build.DISPLAY);
        View buildNumberRow = headerPref.findViewById(R.id.card_build_number);
        if (buildNumberRow != null) {
            buildNumberRow.setOnClickListener(v -> {
                Preference buildPref = findPreference("build_number");
                if (buildPref != null) {
                    buildPref.performClick();
                }
            });
        }

        // SELinux status
        setText(headerPref, R.id.header_selinux_status, getSELinuxStatus());

        // Uptime
        mUptimeView = headerPref.findViewById(R.id.header_uptime);

        // More button
        View moreCard = headerPref.findViewById(R.id.card_more);
        if (moreCard != null) {
            moreCard.setOnClickListener(v -> {
                Preference morePref = findPreference("about_phone_more");
                if (morePref != null) {
                    morePref.performClick();
                }
            });
        }
    }

    private void setText(LayoutPreference pref, int viewId, CharSequence text) {
        TextView tv = pref.findViewById(viewId);
        if (tv != null) {
            tv.setText(text);
        }
    }

    private void setVisible(LayoutPreference pref, int viewId) {
        View v = pref.findViewById(viewId);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void openSecurityBulletin() {
        Intent intent = new Intent(Intent.ACTION_VIEW, SECURITY_BULLETIN_URI);
        PackageManager pm = getContext().getPackageManager();
        if (!pm.queryIntentActivities(intent, 0).isEmpty()) {
            startActivity(intent);
        }
    }

    private static String getMaintainer() {
        String encoded = SystemProperties.get("ro.custom.build.device.maintainer", "");
        if (TextUtils.isEmpty(encoded)) return "";
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private String getSELinuxStatus() {
        if (!SELinux.isSELinuxEnabled()) {
            return getString(R.string.selinux_status_disabled);
        } else if (!SELinux.isSELinuxEnforced()) {
            return getString(R.string.selinux_status_permissive);
        }
        return getString(R.string.selinux_status_enforcing);
    }

    private static String getVendorSecurityPatch() {
        String patch = SystemProperties.get("ro.vendor.build.security_patch", "");
        if (TextUtils.isEmpty(patch)) return "";
        try {
            String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
            return DateFormat.format(format,
                    new SimpleDateFormat("yyyy-MM-dd").parse(patch)).toString();
        } catch (ParseException e) {
            return patch;
        }
    }

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return null;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.my_device_info) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context, null /* lifecycle */);
                }
            };
}
