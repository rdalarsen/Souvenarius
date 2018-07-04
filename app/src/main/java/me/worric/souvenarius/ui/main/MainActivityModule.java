package me.worric.souvenarius.ui.main;

import android.content.Context;

import dagger.Binds;
import dagger.Module;
import me.worric.souvenarius.di.ActivityContext;
import me.worric.souvenarius.di.ActivityScope;

@Module
public abstract class MainActivityModule {

    @Binds
    @ActivityScope
    @ActivityContext
    abstract Context bindContext(MainActivity mainActivity);

}
