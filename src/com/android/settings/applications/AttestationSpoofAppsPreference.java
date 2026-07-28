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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.preference.Preference;

import com.android.settings.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Preference that lets the user opt individual apps into the attestation /
 * build-property spoof layer. The selection is stored as a JSON array of
 * package names in {@link Settings.Secure#ATTESTATION_SPOOF_PACKAGES} and
 * read by the framework when each app process starts, so changes take
 * effect for newly started apps only.
 */
public class AttestationSpoofAppsPreference extends Preference {

    public AttestationSpoofAppsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {
        int count = getSelectedPackages().size();
        return getContext().getResources().getQuantityString(
                R.plurals.attestation_spoof_apps_summary, count, count);
    }

    @Override
    protected void onClick() {
        super.onClick();

        final PackageManager pm = getContext().getPackageManager();
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> activities = pm.queryIntentActivities(launcherIntent, 0);

        final List<String> labels = new ArrayList<>();
        final List<String> packages = new ArrayList<>();
        for (ResolveInfo ri : activities) {
            String pkg = ri.activityInfo.packageName;
            if (packages.contains(pkg)) {
                continue;
            }
            packages.add(pkg);
            ApplicationInfo appInfo = ri.activityInfo.applicationInfo;
            CharSequence label = appInfo.loadLabel(pm);
            labels.add(label + " (" + pkg + ")");
        }
        // Sort by label for usability
        Integer[] order = new Integer[labels.size()];
        for (int i = 0; i < order.length; i++) order[i] = i;
        java.util.Arrays.sort(order, (a, b) -> labels.get(a)
                .compareToIgnoreCase(labels.get(b)));

        final Set<String> selected = getSelectedPackages();
        final String[] orderedLabels = new String[order.length];
        final String[] orderedPackages = new String[order.length];
        final boolean[] checked = new boolean[order.length];
        for (int i = 0; i < order.length; i++) {
            orderedLabels[i] = labels.get(order[i]);
            orderedPackages[i] = packages.get(order[i]);
            checked[i] = selected.contains(orderedPackages[i]);
        }

        new AlertDialog.Builder(getContext())
                .setTitle(getTitle())
                .setMultiChoiceItems(orderedLabels, checked, (dialog, which, isChecked) ->
                        checked[which] = isChecked)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    JSONArray arr = new JSONArray();
                    for (int i = 0; i < orderedPackages.length; i++) {
                        if (checked[i]) {
                            arr.put(orderedPackages[i]);
                        }
                    }
                    Settings.Secure.putString(getContext().getContentResolver(),
                            Settings.Secure.ATTESTATION_SPOOF_PACKAGES,
                            arr.length() > 0 ? arr.toString() : null);
                    notifyChanged();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private Set<String> getSelectedPackages() {
        Set<String> result = new HashSet<>();
        String json = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ATTESTATION_SPOOF_PACKAGES);
        if (TextUtils.isEmpty(json)) {
            return result;
        }
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                result.add(arr.optString(i));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }
}
