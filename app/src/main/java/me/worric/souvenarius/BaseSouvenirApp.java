package me.worric.souvenarius;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import com.jakewharton.threetenabp.AndroidThreeTen;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;

/**
 * Base class for the custom Application class. It contains base functionality shared across the
 * respective build types
 */
public abstract class BaseSouvenirApp extends Application implements HasActivityInjector,
        HasServiceInjector {

    @Inject DispatchingAndroidInjector<Activity> mActivityDispatcher;
    @Inject DispatchingAndroidInjector<Service> mServiceDispatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        DaggerSouvenirAppComponent.builder()
                .application(this)
                .build()
                .inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return mActivityDispatcher;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return mServiceDispatcher;
    }

}
