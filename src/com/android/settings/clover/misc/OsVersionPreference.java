/*
 * SPDX-FileCopyrightText: 2025 The halogenOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.clover.misc;

import android.content.Context;
import android.os.SystemProperties;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.CustomEditTextPreferenceCompat;

public class OsVersionPreference extends CustomEditTextPreferenceCompat {
    private static final String TAG = "OsVersionPreference";
    private static final String OS_VERSION_PROPERTY = "persist.sys.sussybox.osversion";

    public OsVersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        updateSummary();
    }

    private void updateSummary() {
        String currentValue = SystemProperties.get(OS_VERSION_PROPERTY, "");
        if (TextUtils.isEmpty(currentValue)) {
            setSummary(getContext().getString(R.string.os_version_default_summary));
        } else {
            setSummary(getContext().getString(R.string.os_version_custom_summary, currentValue));
        }
    }

    private String getCurrentOsVersion() {
        return SystemProperties.get(OS_VERSION_PROPERTY, "");
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        final EditText editText = (EditText) view.findViewById(android.R.id.edit);
        if (editText != null) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setText(getCurrentOsVersion());
            editText.setHint("160000");
            Utils.setEditTextCursorPosition(editText);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            try {
                String newValue = getText();
                if (TextUtils.isEmpty(newValue)) {
                    // Clear the property if empty
                    SystemProperties.set(OS_VERSION_PROPERTY, "");
                } else {
                    // Validate it's a valid number
                    int version = Integer.parseInt(newValue);
                    if (version < 0 || version > 999999) {
                        Log.e(TAG, "OS version out of range: " + version);
                        return;
                    }
                    SystemProperties.set(OS_VERSION_PROPERTY, newValue);
                }
                updateSummary();
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid OS version number", e);
            }
        }
    }
}
