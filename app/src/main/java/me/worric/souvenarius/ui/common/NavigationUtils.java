package me.worric.souvenarius.ui.common;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import me.worric.souvenarius.R;
import me.worric.souvenarius.ui.main.MainFragment;

public final class NavigationUtils {

    public static final int LAUNCHED_FROM_HISTORY_AND_IN_NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;

    private NavigationUtils() {
    }

    public static void buildCustomFragmentNavigation(FragmentManager fm, Fragment customFragment) {
        fm.beginTransaction()
                .replace(R.id.fragment_container, customFragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    public static void buildMainFragmentNavigation(FragmentManager fm) {
        fm.beginTransaction()
                .add(R.id.fragment_container, MainFragment.newInstance())
                .setReorderingAllowed(true)
                .commit();
    }

    public static void normalLaunch(FragmentManager fm) {
        fm.beginTransaction()
                .add(R.id.fragment_container, MainFragment.newInstance())
                .commit();
    }

}
