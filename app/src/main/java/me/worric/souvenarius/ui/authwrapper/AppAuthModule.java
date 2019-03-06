package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseAuth;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class AppAuthModule {

    @Provides
    static FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Binds
    abstract AppAuth bindAppAuth(DefaultAppAuth defaultAppAuth);

}
