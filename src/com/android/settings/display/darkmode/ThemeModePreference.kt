// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package com.android.settings.display.darkmode

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isInvisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settingslib.thememode.R as ThemeModeR
import com.android.settingslib.thememode.ThemeMode
import com.android.settingslib.thememode.ThemeModeState
import com.android.settingslib.thememode.ui.ThemeModeButtons
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class ThemeModePreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    Preference(context, attrs) {

    var state: ThemeModeState? = null
        set(value) {
            field = value
            notifyChanged()
        }

    var onThemeModeClick: (ThemeMode) -> Unit = {}

    init {
        widgetLayoutResource = ThemeModeR.layout.theme_mode_buttons
        isSelectable = false
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val group =
            holder.findViewById(ThemeModeR.id.theme_mode_buttons) as MaterialButtonToggleGroup
        val state = state
        group.isInvisible = state == null
        if (state == null) return
        ThemeModeButtons(group).apply {
            show(state, checkIconId = R.drawable.ic_check_24dp)
            setOnThemeModeClickListener(onThemeModeClick)
            buttons.values.forEach { it.setCheckedStateColors() }
        }
    }

    private fun MaterialButton.setCheckedStateColors() {
        val content = context.getColorStateList(R.color.theme_mode_button_content)
        backgroundTintList = context.getColorStateList(R.color.theme_mode_button_container)
        setTextColor(content)
        iconTint = content
    }
}
