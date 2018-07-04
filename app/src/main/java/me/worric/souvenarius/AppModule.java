package me.worric.souvenarius;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import dagger.multibindings.IntoMap;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.di.ViewModelKey;
import me.worric.souvenarius.ui.common.AppViewModelFactory;
import me.worric.souvenarius.ui.main.MainActivity;
import me.worric.souvenarius.ui.main.MainActivityModule;
import me.worric.souvenarius.ui.main.MainViewModel;

@Module(includes = AndroidSupportInjectionModule.class)
public abstract class AppModule {

    @Binds
    @Singleton
    @AppContext
    abstract Context bindApplicationContext(Application application);

    @ContributesAndroidInjector(modules = MainActivityModule.class)
    abstract MainActivity contributeMainActivity();

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(AppViewModelFactory appViewModelFactory);

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindViewModel(MainViewModel mainViewModel);

    @Provides
    @Named(value = "theMessage")
    static String providesMessage() {
        return "This is the message";
    }

}
