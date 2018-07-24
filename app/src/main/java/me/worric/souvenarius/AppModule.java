package me.worric.souvenarius;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import dagger.multibindings.IntoMap;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.di.ViewModelKey;
import me.worric.souvenarius.ui.add.AddViewModel;
import me.worric.souvenarius.ui.common.AppViewModelFactory;
import me.worric.souvenarius.ui.detail.DetailViewModel;
import me.worric.souvenarius.ui.main.MainActivity;
import me.worric.souvenarius.ui.main.MainActivityModule;
import me.worric.souvenarius.ui.main.MainViewModel;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;

@Module(includes = AndroidSupportInjectionModule.class)
public abstract class AppModule {

    @Binds
    @Singleton
    @AppContext
    abstract Context bindApplicationContext(Application application);

    @ContributesAndroidInjector(modules = MainActivityModule.class)
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector
    abstract UpdateWidgetService contributeUpdateWidgetService();

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(AppViewModelFactory appViewModelFactory);

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindViewModel(MainViewModel mainViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DetailViewModel.class)
    abstract ViewModel bindDetailViewModel(DetailViewModel detailViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AddViewModel.class)
    abstract ViewModel bindAddViewModel(AddViewModel addViewModel);

    @Provides
    @Singleton
    static AppDatabase provideDatabase(@AppContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "souvenirdb").build();
    }

    @Provides
    @Singleton
    static SharedPreferences provideSharedPreferences(@AppContext Context context) {
        return context.getSharedPreferences("skod", Context.MODE_PRIVATE);
    }

}
