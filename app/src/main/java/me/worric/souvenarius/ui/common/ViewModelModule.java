package me.worric.souvenarius.ui.common;

import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import me.worric.souvenarius.di.ActivityScope;

@Module
public abstract class ViewModelModule {

    @Binds
    @ActivityScope
    abstract ViewModelProvider.Factory bindViewModelFactory(AppViewModelFactory appViewModelFactory);

}
