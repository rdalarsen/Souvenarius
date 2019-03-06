package me.worric.souvenarius.data;

import android.content.Context;

import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.di.AppContext;

@Module
public abstract class RoomMockModule {

    @Provides
    @Singleton
    static AppDatabase provideDatabase(@AppContext Context context) {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .addCallback(RoomMockUtils.sCallback).build();
    }

}
