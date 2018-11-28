package me.worric.souvenarius;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = SouvenirAppModule.class)
public interface SouvenirAppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        SouvenirAppComponent build();
    }

    void inject(BaseSouvenirApp app);

}
