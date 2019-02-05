package me.worric.souvenarius.ui.main;

import android.content.Context;
import androidx.fragment.app.FragmentManager;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import me.worric.souvenarius.di.ActivityContext;
import me.worric.souvenarius.di.ActivityScope;

@Module
public abstract class MainActivityModule {

    @Binds
    @ActivityScope
    @ActivityContext
    abstract Context bindContext(MainActivity mainActivity);

    @Binds
    abstract Navigator bindNavigator(NavigatorImpl impl);

    @Provides
    static FragmentManager provideFragmentManager(MainActivity mainActivity) {
        return mainActivity.getSupportFragmentManager();
    }

}
