package com.android.settings.xos.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationPanelTweaksStorage {
    
    /* Variable storage */

    /* Class section */
    
    private Context myContext;

    public SharedPreferences mSharedPreferences;

    public NotificationPanelTweaksStorage(Context context) {
        myContext = context;
        init();
    }

    public void init() {
        mSharedPreferences = getContext()
            .getSharedPreferences("notification_panel_tweaks", 0);
        // here you can init prefs
    }

    
    /* General methods */

    private Context getContext() {
        return myContext;
    }

    public String getPref(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public boolean getPref(String key, boolean defaultValue) {
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public int getPref(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public void setPref(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setPref(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setPref(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void rmPref(String key) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void clearPrefs() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


    /**
     * Keys for shared preferences
     */
    public static final class BPrefKeys {
        public static final String
                    nothing = "this_is_a_key_which_is_a_placeholder"
                        ;
    }

}