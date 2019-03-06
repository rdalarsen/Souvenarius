package me.worric.souvenarius.ui.authwrapper;

import java.util.Map;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class InMemoryAppAuthModule {

    @Provides
    static Map<AppUser,String> provideTestData() {
        return InMemoryAppAuth.MockData.get();
    }

    @Binds
    abstract AppAuth bindMockAppAuth(InMemoryAppAuth inMemoryAppAuth);

}
