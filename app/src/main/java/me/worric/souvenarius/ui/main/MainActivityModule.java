package me.worric.souvenarius.ui.main;

import android.content.Context;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.worric.souvenarius.di.ActivityContext;
import me.worric.souvenarius.di.ActivityScope;
import me.worric.souvenarius.ui.add.AddFragment;

@Module
public abstract class MainActivityModule {

    @Binds
    @ActivityScope
    @ActivityContext
    abstract Context bindContext(MainActivity mainActivity);

    @ActivityScope
    @ContributesAndroidInjector
    abstract AddFragment contributeAddFragment();

}
