/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.settings.dashboard.suggestions

import android.app.settings.SettingsEnums
import android.content.Context
import android.os.Bundle
import android.service.settings.suggestions.Suggestion
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.core.InstrumentedFragment
import com.android.settings.homepage.SplitLayoutListener
import com.android.settingslib.suggestions.SuggestionController

/**
 * Fragment to control display and interaction logic for [Suggestion]s
 */
class SuggestionFragment : InstrumentedFragment(),
    SplitLayoutListener, SuggestionController.ServiceConnectionListener {

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun getMetricsCategory(): Int {
        return SettingsEnums.SETTINGS_HOMEPAGE
    }

    override fun onServiceConnected() {
        // no-op
    }

    override fun onServiceDisconnected() {
        // no-op
    }

    override fun setSplitLayoutSupported(supported: Boolean) {
        // no-op
    }

    override fun onSplitLayoutChanged(isRegularLayout: Boolean) {
        // no-op
    }
}
