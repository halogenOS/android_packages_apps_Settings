/*
 * Copyright (C) 2017 The halogenOS Project
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

package com.android.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.animation.AccelerateInterpolator;
import com.android.internal.util.omni.PackageUtils;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.R;

import com.plattysoft.leonids.ParticleSystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;

public class CustomizationsActivity extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        Indexable {
    private static final String TAG = CustomizationsActivity.class.getSimpleName();


    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.XOS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.customizations);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return onPreferenceChange(preference, null);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (PackageUtils.isImageTileInstalled(getContext())){
          Random rand =  new Random();
          int firstRandom = rand.nextInt(91 - 0);
          int secondRandom = rand.nextInt(181 - 90);
          int thirdRandom = rand.nextInt(181 - 0);
          Drawable flyingFucks = super.getResources().getDrawable(R.drawable.flying_fucks, null);
          ParticleSystem ps = new ParticleSystem(getActivity(), 100, flyingFucks, 3000);
          ps.setScaleRange(0.7f,1.3f);
          ps.setSpeedRange(0.1f, 0.25f);
          ps.setAcceleration(0.0001f, thirdRandom);
          ps.setRotationSpeedRange(firstRandom, secondRandom);
          ps.setFadeOut(200, new AccelerateInterpolator());
          ps.oneShot(this.getView(), 100);
        }
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_customizations;
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
                   Settings.System.NAVIGATION_BAR_ENABLED, 0) != 0;
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
    /** Index provider used to expose this fragment in search. */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                    final Resources res = context.getResources();
                    final SearchIndexableRaw data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.customizations_title);
                    data.screenTitle = res.getString(R.string.customizations_title);
                    data.keywords = res.getString(R.string.customizations_keywords);

                    final List<SearchIndexableRaw> result = new ArrayList<>(1);
                    result.add(data);
                    return result;
                }

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.customizations;
                    result.add(sir);

                    return result;
                }
            };
}
