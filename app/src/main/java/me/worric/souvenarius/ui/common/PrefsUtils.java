package me.worric.souvenarius.ui.common;

import android.content.SharedPreferences;

import me.worric.souvenarius.ui.main.SortStyle;
import timber.log.Timber;

public final class PrefsUtils {

    public static final String DEFAULT_SORT_VALUE = SortStyle.DESC.toString();

    private PrefsUtils() {
    }

    public static SortStyle getSortStyleFromPrefs(SharedPreferences sharedPreferences, String key) {
        Timber.i("getting sortStyle from prefs. Key is: %s, and value is: %s", key, sharedPreferences.getString(key, DEFAULT_SORT_VALUE));
        String value = sharedPreferences.getString(key, SortStyle.DESC.toString());
        return SortStyle.valueOf(value);
    }

}
