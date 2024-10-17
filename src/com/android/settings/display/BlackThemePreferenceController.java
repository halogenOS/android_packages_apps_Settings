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
 * limitations under the License
 */

package com.android.settings.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.OverlayIdentifier;
import android.content.om.OverlayInfo;
import android.content.om.OverlayManager;
import android.content.om.OverlayManagerTransaction;
import android.content.res.Configuration;
import android.os.UserHandle;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import java.util.List;

public class BlackThemePreferenceController extends TogglePreferenceController implements LifecycleObserver {

    @VisibleForTesting
    Preference mPreference;

    private OverlayManager mOverlayManager;
    private final String blackThemeOverlay;
    private final Context mContext;

    public BlackThemePreferenceController(Context context, String key) {
        super(context, key);
        mContext = context;
        mOverlayManager = (OverlayManager) context.getSystemService(Context.OVERLAY_SERVICE);
        blackThemeOverlay = context.getResources().getString(R.string.config_blackThemePackage);
    }

    @Override
    public boolean isChecked() {
        OverlayInfo overlayInfo = mOverlayManager.getOverlayInfo(blackThemeOverlay, UserHandle.CURRENT);
        return overlayInfo != null && overlayInfo.isEnabled();
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        // Inspired by https://github.com/DerpFest-AOSP/vendor_support/blob/3ef52d7f7c59a1885989cb86992b6b16d52c797c/src/org/derpfest/support/preferences/OverlaySwitchPreference.java#L77
        final boolean isNight =
            (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        OverlayManagerTransaction.Builder tx = new OverlayManagerTransaction.Builder();
        tx.setEnabled(
            mOverlayManager.getOverlayInfo(blackThemeOverlay, UserHandle.CURRENT).getOverlayIdentifier(),
            isChecked && isNight, UserHandle.USER_CURRENT);

        final List<OverlayInfo> overlays = mOverlayManager.getOverlayInfosForTarget("android", UserHandle.CURRENT);
        for (OverlayInfo info : overlays) {
            if ("neutral".equals(info.getOverlayName())) {
                tx.setEnabled(info.getOverlayIdentifier(), !isChecked || !isNight, UserHandle.USER_CURRENT);
                break;
            }
        }

        mOverlayManager.commit(tx.build());
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
}
