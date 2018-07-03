package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModel;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import me.worric.souvenarius.di.ActivityContext;
import me.worric.souvenarius.di.ActivityScope;
import me.worric.souvenarius.di.ViewModelKey;

@Module
public abstract class MainActivityModule {

    @Binds
    @ActivityScope
    @ActivityContext
    abstract Context bindContext(MainActivity mainActivity);

}
