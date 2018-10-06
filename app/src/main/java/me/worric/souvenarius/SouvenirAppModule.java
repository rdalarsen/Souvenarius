package me.worric.souvenarius;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import me.worric.souvenarius.data.DataModule;
import me.worric.souvenarius.data.repository.UpdateSouvenirsService;
import me.worric.souvenarius.di.ActivityScope;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.ui.FragmentContributorModule;
import me.worric.souvenarius.ui.common.PrefsUtils;
import me.worric.souvenarius.ui.common.ViewModelModule;
import me.worric.souvenarius.ui.main.MainActivity;
import me.worric.souvenarius.ui.main.MainActivityModule;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;

@Module(includes = {AndroidSupportInjectionModule.class, ViewModelModule.class, DataModule.class})
public abstract class SouvenirAppModule {

    @Binds
    @Singleton
    @AppContext
    abstract Context bindApplicationContext(Application application);

    @ActivityScope
    @ContributesAndroidInjector(modules = {MainActivityModule.class, FragmentContributorModule.class})
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector
    abstract UpdateWidgetService contributeUpdateWidgetService();

    @ContributesAndroidInjector
    abstract UpdateSouvenirsService contributeUpdateSouvenirsService();

    @Provides
    @Singleton
    static SharedPreferences provideSharedPreferences(@AppContext Context context) {
        return context.getSharedPreferences(PrefsUtils.PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    static Handler provideMainLooperHandler() {
        return new Handler(Looper.getMainLooper());
    }

}
