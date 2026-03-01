/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import static com.android.settings.core.BasePreferenceController.AVAILABLE;
import static com.android.settings.core.BasePreferenceController.UNSUPPORTED_ON_DEVICE;
import static com.android.settings.deviceinfo.DeviceNamePreferenceController.RES_SHOW_DEVICE_NAME_BOOL;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceNamePreferenceControllerTest {
    private static final String TEST_PREFERENCE_KEY = "test_key";

    private DeviceNamePreferenceController mController;
    private Context mContext;
    private Resources mResources;


    @Before
    public void setUp() {
        mContext = spy(ApplicationProvider.getApplicationContext());
        mResources = spy(mContext.getResources());
        when(mContext.getResources()).thenReturn(mResources);

        mController = new DeviceNamePreferenceController(mContext, TEST_PREFERENCE_KEY);
    }

    @After
    public void tearDown() {
        Settings.Global.putString(
                mContext.getContentResolver(), Settings.Global.DEVICE_NAME, null);
    }

    @Test
    public void getAvailibilityStatus_availableByDefault() {
        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void getAvailabilityStatus_unsupportedWhenSet() {
        doReturn(false).when(mResources).getBoolean(RES_SHOW_DEVICE_NAME_BOOL);
        assertThat(mController.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void constructor_defaultDeviceNameIsModelName() {
        assertThat(mController.getSummary()).isEqualTo(Build.MODEL);
    }

    @Test
    public void constructor_deviceNameLoadedIfSet() {
        Settings.Global.putString(
                mContext.getContentResolver(), Settings.Global.DEVICE_NAME, "Test");
        mController = new DeviceNamePreferenceController(mContext, "test_key");
        assertThat(mController.getSummary()).isEqualTo("Test");
    }
}
