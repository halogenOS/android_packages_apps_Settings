/*
 * Copyright (C) 2019 The Android Open Source Project
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

 package com.android.settings.deviceinfo.firmwareversion;

 import android.content.Context;
 import android.net.Uri;
 import android.os.SystemProperties;
 import android.text.format.DateFormat;
 import android.text.TextUtils;
 import android.util.Log;
 
 import androidx.preference.Preference;
 
 import com.android.settings.core.BasePreferenceController;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 
 public class VendorSecurityPatchLevelPreferenceController extends BasePreferenceController {
 
     private static final String TAG = "VendorSecurityPatchCtrl";
 
     private final String mCurrentPatch;
 
     public VendorSecurityPatchLevelPreferenceController(Context context, String key) {
         super(context, key);
         mCurrentPatch = getVendorSecurityPatch();
     }
 
     private static String getVendorSecurityPatch() {
         String patch = SystemProperties.get("ro.vendor.build.security_patch");
         if (!"".equals(patch)) {
             try {
                 String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                 patch = DateFormat.format(format,
                             new SimpleDateFormat("yyyy-MM-dd").parse(patch)).toString();
             } catch (ParseException e) {
                 // broken parse; fall through and use the raw string
             }
             return patch;
         } else {
             return "";
         }
     }
 
     @Override
     public int getAvailabilityStatus() {
         return !TextUtils.isEmpty(mCurrentPatch)
                 ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
     }
 
     @Override
     public CharSequence getSummary() {
         return mCurrentPatch;
     }
 
     @Override
     public boolean handlePreferenceTreeClick(Preference preference) {
         if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
             return false;
         }
         return true;
     }
 }
 