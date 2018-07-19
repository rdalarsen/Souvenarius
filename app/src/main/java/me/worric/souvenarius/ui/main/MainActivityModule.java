package me.worric.souvenarius.ui.main;

import android.content.Context;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.worric.souvenarius.di.ActivityContext;
import me.worric.souvenarius.di.ActivityScope;
import me.worric.souvenarius.ui.add.AddFragment;
import me.worric.souvenarius.ui.detail.DetailFragment;
import me.worric.souvenarius.ui.detail.EditDialogFragment;

@Module
public abstract class MainActivityModule {

    @Binds
    @ActivityScope
    @ActivityContext
    abstract Context bindContext(MainActivity mainActivity);

    @ActivityScope
    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract AddFragment contributeAddFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract DetailFragment contributeDetailFragment();

    @ActivityScope
    @ContributesAndroidInjector
    abstract EditDialogFragment contributeEditDiablogFragment();

}
