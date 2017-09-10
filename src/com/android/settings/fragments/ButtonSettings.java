/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2016-2017 The halogenOS Project
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

package com.android.settings.fragments;

import static org.halogenos.hardware.buttons.IButtonBacklightControl.CONTROL_TYPE_FULL;
import static org.halogenos.hardware.buttons.IButtonBacklightControl.CONTROL_TYPE_PARTIAL;
import static org.halogenos.hardware.buttons.IButtonBacklightControl.CONTROL_TYPE_SWITCH;
import static org.halogenos.hardware.buttons.IButtonBacklightControl.CONTROL_TYPE_NONE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.view.RotationPolicy;
import com.android.settings.accessibility.ToggleFontSizePreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

import org.halogenos.hardware.buttons.IButtonBacklightControl;
import org.halogenos.hardware.buttons.KeyDisablerUtils;

public class ButtonSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        Indexable {
    private static final String TAG = ButtonSettings.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String KEY_SHOW_NAVBAR = "buttons_show_navbar";
    private static final String KEY_HW_BUTTONS = "buttons_enable_hw_buttons";
    private static final String KEY_HW_BACKLIGHT = "buttons_hw_backlight";

    private static final int NAVBAR_MUST_SHOW = -2;
    private static final int NAVBAR_NOT_SET = -1;
    private static final int NAVBAR_DONT_SHOW = 0;
    private static final int NAVBAR_SHOW = 1;

    private SwitchPreference mShowNavbarPreference;
    private SwitchPreference mEnableHwButtonsPreference;

    private Preference mHwBacklightPreference;

    private Handler mHandler;
    private final Runnable resetNavbarToggle = new Runnable() {
        @Override
        public void run() {
            mShowNavbarPreference.setEnabled(true);
        }
    };

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        final Resources res = getActivity().getResources();

        addPreferencesFromResource(R.xml.button_settings);

        mShowNavbarPreference = (SwitchPreference)
                findPreference(KEY_SHOW_NAVBAR);

        mShowNavbarPreference.setOnPreferenceChangeListener(this);

        if(KeyDisablerUtils.areHwKeysSupported()) {
            mEnableHwButtonsPreference = (SwitchPreference)
                findPreference(KEY_HW_BUTTONS);
            mEnableHwButtonsPreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_HW_BUTTONS);
        }

        int currentControlType =
                IButtonBacklightControl.getCurrentControlType(getContentResolver());
        if (currentControlType == CONTROL_TYPE_NONE || !KeyDisablerUtils.areHwKeysSupported()) {
            // No button backlight control
            removePreference(KEY_HW_BACKLIGHT);
        } else {
            mHwBacklightPreference = findPreference(KEY_HW_BACKLIGHT);
            mHwBacklightPreference.setOnPreferenceClickListener(this);
        }

        updateState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        updateState(KEY_SHOW_NAVBAR);
        updateState(KEY_HW_BUTTONS);
        updateState(KEY_HW_BACKLIGHT);
    }

    private void updateState(String key) {
        if(DEBUG) Log.d(TAG, "Updating state for " + key);
        switch(key) {
            case KEY_SHOW_NAVBAR:
                int nav = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_ENABLED, -1, UserHandle.USER_CURRENT);
                mShowNavbarPreference.setChecked(nav == NAVBAR_SHOW);
            case KEY_HW_BUTTONS:
                if(mEnableHwButtonsPreference == null) break;
                mEnableHwButtonsPreference.setChecked(
                    Settings.System.getIntForUser(getContentResolver(),
                        Settings.System.HARDWARE_BUTTONS_ENABLED, 0,
                        UserHandle.USER_CURRENT) == 1
                );
                break;
            case KEY_HW_BACKLIGHT:
                autoSetBacklightPrefPercentage();
                break;
            default: break;
        }
    }

    public void setBacklightPrefPercentage(int perc) {
        if(mHwBacklightPreference != null)
            mHwBacklightPreference.setSummary(
                perc < 0 ? doMagicOnBrightnessMagic(perc) :
                    String.format(
                        getString(R.string.buttons_button_backlight_status),
                        String.valueOf(perc) + " %"
                    )
            );
    }

    public String doMagicOnBrightnessMagic(int magic) {
        return
            magic == -2 ?
                getString(R.string.general_on) :
                (magic == -3 ?
                    getString(R.string.general_off) :
                    (magic == -1 ?
                        getString(R.string.general_dimmed) :
                            String.valueOf(magic)));
    }

    public int doMagicOnBrightnessProgress(int cb, int cc) {
        return cc == CONTROL_TYPE_SWITCH ? (cb == 1000 ? -2 : -3)
                : (cc == CONTROL_TYPE_FULL ?
                    cb / 10 : (cb == 1 ? -1 : (cb == 2 ? -2 :
                        (cb == 1000 ? -2 : -3))));
    }

    public void applyBacklightPrefPercentage(int cb) {
        int cc = IButtonBacklightControl.getCurrentControlType(getContentResolver());
        setBacklightPrefPercentage(
            doMagicOnBrightnessProgress(cb, cc));
    }

    public void autoSetBacklightPrefPercentage() {
        applyBacklightPrefPercentage(Settings.System.getIntForUser(getContentResolver(),
            Settings.System.BUTTON_BACKLIGHT_BRIGHTNESS, 0,
            UserHandle.USER_CURRENT));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return onPreferenceChange(preference, null);
    }

    private void applyNavbarSetting(boolean enable) {
        Settings.System.putIntForUser(getContentResolver(),
            Settings.System.NAVIGATION_BAR_ENABLED, enable ? 1 : 0,
            UserHandle.USER_CURRENT);
    }

    private void showBacklightDialog() {
        final ContentResolver resolver = getContentResolver();
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle(R.string.buttons_button_backlight_title)
         .setPositiveButton(android.R.string.ok,
             new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface d, int i) {
                     d.dismiss();
                 }
             })
          .setCancelable(true);

        LinearLayout layout = new LinearLayout(getContext());
        LayoutInflater.from(getContext()).inflate(R.layout.button_backlight_dialog, layout);
        LinearLayout onoffl = (LinearLayout) layout.findViewById(R.id.button_backlight_dialog_onoff);
        LinearLayout brightnessl = (LinearLayout) layout.findViewById(R.id.button_backlight_dialog_brightness);
        SeekBar brightness = (SeekBar) layout.findViewById(R.id.button_backlight_dialog_brightness_bar);
        final SeekBar timeout = (SeekBar) layout.findViewById(R.id.button_backlight_dialog_timeout_bar);
        Switch onoff = (Switch) layout.findViewById(R.id.button_backlight_dialog_onoff_switch);
        TextView onofftv = (TextView) layout.findViewById(R.id.button_backlight_dialog_onoff_text);
        final TextView brightntv = (TextView) layout.findViewById(R.id.button_backlight_dialog_brightness_tv);
        final TextView timeouttv = (TextView) layout.findViewById(R.id.button_backlight_dialog_timeout_tv);

        int bftb = 0;
        final int cc = IButtonBacklightControl.getCurrentControlType(resolver);
        if(cc == CONTROL_TYPE_FULL || cc == CONTROL_TYPE_PARTIAL) {
            onoffl.setVisibility(View.GONE);
            layout.removeView(onoffl);
            onoffl = null;
            onoff  = null;
        } else if(cc == CONTROL_TYPE_SWITCH) {
            brightnessl.setVisibility(View.GONE);
            layout.removeView(brightnessl);
            brightnessl = null;
            brightness  = null;
            bftb = Settings.System.getIntForUser(getContentResolver(),
                        Settings.System.BUTTON_BACKLIGHT_BRIGHTNESS,
                        0, UserHandle.USER_CURRENT);
            onoff.setChecked(bftb == 1000);
        } else return;

        if(cc == CONTROL_TYPE_SWITCH)
            onoff.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean ch) {
                        Settings.System.putIntForUser(resolver,
                            Settings.System.BUTTON_BACKLIGHT_BRIGHTNESS,
                            ch ? 1000 : 0, UserHandle.USER_CURRENT);
                        applyBacklightPrefPercentage(ch ? 1000 : 0);
                    }
                });
        if(cc != CONTROL_TYPE_SWITCH) {
            brightness.setMax(
                cc == CONTROL_TYPE_FULL ? 1000 : 2);
            bftb = Settings.System.getIntForUser(getContentResolver(),
                                    Settings.System.BUTTON_BACKLIGHT_BRIGHTNESS,
                                    0, UserHandle.USER_CURRENT);
            brightness.setProgress(bftb);
            brightntv.setText(doMagicOnBrightnessMagic(
                                doMagicOnBrightnessProgress(bftb, cc)));
            brightness.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    /// Notification that the progress level has changed.
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar, int progress, boolean fromUser) {
                        if(!fromUser) return;
                        brightntv.setText(
                            doMagicOnBrightnessMagic(
                                doMagicOnBrightnessProgress(progress, cc)));
                        timeout.setEnabled(progress > 0);
                        Settings.System.putIntForUser(resolver,
                            Settings.System.BUTTON_BACKLIGHT_BRIGHTNESS,
                            progress, UserHandle.USER_CURRENT);
                        applyBacklightPrefPercentage(progress);
                    }
                    /// Notification that the user has started a touch gesture.
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Not needed
                    }
                    /// Notification that the user has finished a touch gesture.
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        updateState(KEY_HW_BACKLIGHT);
                    }
                });
        }

        timeout.setMax(10);
        int tmot = Settings.System.getIntForUser(resolver,
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 3,
                    UserHandle.USER_CURRENT) - 1;
        timeout.setProgress(tmot == -2 ? 10 : tmot);
        timeouttv.setText(tmot == -2 ? "-" : String.valueOf(tmot + 1));
        timeout.setEnabled(bftb != 0);
        timeout.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                    /// Notification that the progress level has changed.
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar, int oprogress, boolean fromUser) {
                        if(!fromUser) return;
                        int progress = oprogress + 1;
                        timeouttv.setText(
                            progress == 11 ? "-" : String.valueOf(progress));
                        Settings.System.putIntForUser(resolver,
                            Settings.System.BUTTON_BACKLIGHT_TIMEOUT,
                            (progress == 11 ? -1 : progress), UserHandle.USER_CURRENT);
                    }
                    /// Notification that the user has started a touch gesture.
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Not needed
                    }
                    /// Notification that the user has finished a touch gesture.
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Not needed
                    }
                });

        b.setView(layout);
        b.create().show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if(DEBUG) Log.d(TAG, "onPreferenceChange: " + preference.getKey() +
                    " : " + objValue);
        String key = preference.getKey();
        switch(key) {
            case KEY_SHOW_NAVBAR:
                applyNavbarSetting((boolean)objValue);
                mShowNavbarPreference.setEnabled(false);
                mHandler.postDelayed(resetNavbarToggle, 480);
            case KEY_HW_BUTTONS:
                boolean newSetting = (key.equals(KEY_HW_BUTTONS)
                    ? (boolean)objValue : !(boolean)objValue);
                if(mEnableHwButtonsPreference == null) break;
                Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HARDWARE_BUTTONS_ENABLED,
                        newSetting ? 1 : 0,
                    UserHandle.USER_CURRENT);
                break;
            case KEY_HW_BACKLIGHT:
                showBacklightDialog();
                break;
            default: break;
        }
        updateState(preference.getKey());
        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_uri_buttons;
    }

}
