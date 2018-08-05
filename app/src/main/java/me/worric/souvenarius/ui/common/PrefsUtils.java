package me.worric.souvenarius.ui.common;

import android.content.SharedPreferences;

import me.worric.souvenarius.ui.main.SortStyle;

public final class PrefsUtils {

    public static final String PREFS_NAME = "souvenarius";
    public static final String PREFS_KEY_SORT_STYLE = "sortStyle";
    private static final String DEFAULT_SORT_VALUE = SortStyle.DESC.toString();

    private PrefsUtils() {
    }

    public static SortStyle getSortStyleFromPrefs(SharedPreferences sharedPreferences, String key) {
        String value = sharedPreferences.getString(key, DEFAULT_SORT_VALUE);
        return SortStyle.valueOf(value);
    }

}
