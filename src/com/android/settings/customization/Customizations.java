/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2016-17 The halogenOS Project
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

package com.android.settings.customization;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.PreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Customizations extends DashboardFragment {

    private static final String TAG = Customizations.class.getSimpleName();

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.XOS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.customizations;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        return null;
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final SummaryLoader mSummaryLoader;
        private ContentResolver resolver;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
            resolver = mContext.getContentResolver();
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                boolean navbarEnabled = Settings.System.getInt(resolver,
                   Settings.Secure.NAVIGATION_BAR_ENABLED, 0) != 0;
                String newSummary = navbarEnabled ? mContext.getString(R.string.navbar_enabled)
                                              : mContext.getString(R.string.navbar_disabled);
                mSummaryLoader.setSummary(this, newSummary);
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
}