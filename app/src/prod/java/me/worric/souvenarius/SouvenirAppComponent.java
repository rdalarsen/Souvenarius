package me.worric.souvenarius;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import me.worric.souvenarius.data.DataModule;
import me.worric.souvenarius.data.RoomProdModule;
import me.worric.souvenarius.ui.FragmentContributorModule;
import me.worric.souvenarius.ui.authwrapper.FirebaseAppAuthModule;
import me.worric.souvenarius.ui.common.ViewModelModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        SouvenirAppModule.class,
        ViewModelModule.class,
        FragmentContributorModule.class,
        FirebaseAppAuthModule.class,
        RoomProdModule.class,
        DataModule.class
})
public interface SouvenirAppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        SouvenirAppComponent build();
    }

    void inject(BaseSouvenirApp app);

}
