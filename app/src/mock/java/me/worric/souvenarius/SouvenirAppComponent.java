package me.worric.souvenarius;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import me.worric.souvenarius.data.DataModule;
import me.worric.souvenarius.data.RoomMockModule;
import me.worric.souvenarius.ui.FragmentContributorModule;
import me.worric.souvenarius.ui.authwrapper.InMemoryAppAuthModule;
import me.worric.souvenarius.ui.common.ViewModelModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        SouvenirAppModule.class,
        ViewModelModule.class,
        FragmentContributorModule.class,
        InMemoryAppAuthModule.class,
        RoomMockModule.class,
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
