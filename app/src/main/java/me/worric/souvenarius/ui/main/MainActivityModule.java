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

    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragment();

    @ContributesAndroidInjector
    abstract AddFragment contributeAddFragment();

    @ContributesAndroidInjector
    abstract DetailFragment contributeDetailFragment();

    @ContributesAndroidInjector
    abstract EditDialogFragment contributeEditDiablogFragment();

}
