/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.applications;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.nullable;
import static org.mockito.Mockito.verify;

import android.content.Context;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.testutils.SettingsRobolectricTestRunner;
import com.android.settings.TestConfig;
import com.android.settings.core.TouchOverlayManager;
import com.android.settings.testutils.FakeFeatureFactory;
import com.android.settings.testutils.shadow.ShadowPreferenceFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ReflectionHelpers;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class DrawOverlayDetailsTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Context mContext;

    private FakeFeatureFactory mFeatureFactory;
    private DrawOverlayDetails mFragment;

    @Mock
    private TouchOverlayManager mTouchOverlayManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        FakeFeatureFactory.setupForTest(mContext);
        mFeatureFactory = (FakeFeatureFactory) FakeFeatureFactory.getFactory(mContext);

        mFragment = new DrawOverlayDetails();
        ReflectionHelpers.setField(mFragment, "mTouchOverlayManager", mTouchOverlayManager);
    }

    @Test
    public void logSpecialPermissionChange() {
        mFragment.onAttach(ShadowApplication.getInstance().getApplicationContext());
        mFragment.logSpecialPermissionChange(true, "app");
        verify(mFeatureFactory.metricsFeatureProvider).action(nullable(Context.class),
                eq(MetricsProto.MetricsEvent.APP_SPECIAL_PERMISSION_APPDRAW_ALLOW), eq("app"));

        mFragment.logSpecialPermissionChange(false, "app");
        verify(mFeatureFactory.metricsFeatureProvider).action(nullable(Context.class),
                eq(MetricsProto.MetricsEvent.APP_SPECIAL_PERMISSION_APPDRAW_DENY), eq("app"));
    }

    @Test
    @Config(shadows = ShadowPreferenceFragment.class)
    public void onStart_disableOverlay() {
        mFragment.onStart();
        verify(mTouchOverlayManager).setOverlayAllowed(false);
    }

    @Test
    @Config(shadows = ShadowPreferenceFragment.class)
    public void onStop_enableOverlay() {
        mFragment.onStop();
        verify(mTouchOverlayManager).setOverlayAllowed(true);
    }
}
