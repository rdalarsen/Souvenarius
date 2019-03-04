package me.worric.souvenarius;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * Specialization of the custom Application base class that adds debug functionality for the debug
 * build type
 */
public class SouvenirApp extends BaseSouvenirApp {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Stetho.initializeWithDefaults(this);
    }

}
