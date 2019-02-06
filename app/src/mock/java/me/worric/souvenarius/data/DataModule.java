package me.worric.souvenarius.data;

import android.content.Context;
import android.location.Geocoder;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.repository.location.LocationRepository;
import me.worric.souvenarius.data.repository.location.LocationRepositoryImpl;
import me.worric.souvenarius.data.repository.location.LocationTaskRunner;
import me.worric.souvenarius.data.repository.location.LocationTaskRunnerImpl;
import me.worric.souvenarius.data.repository.souvenir.DbTaskRunner;
import me.worric.souvenarius.data.repository.souvenir.DbTaskRunnerImpl;
import me.worric.souvenarius.data.repository.souvenir.FirebaseHandler;
import me.worric.souvenarius.data.repository.souvenir.MockFirebaseHandlerImpl;
import me.worric.souvenarius.data.repository.souvenir.MockStorageHandlerImpl;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepositoryImpl;
import me.worric.souvenarius.data.repository.souvenir.StorageHandler;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.di.FirebaseErrorMsgs;
import me.worric.souvenarius.di.LocationErrorMsgs;
import me.worric.souvenarius.di.SouvenirErrorMsgs;

@Module
public abstract class DataModule {

    @Provides
    @Singleton
    static AppDatabase provideDatabase(@AppContext Context context) {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .addCallback(AppDatabase.sCallback).build();
    }

    @Binds
    abstract SouvenirRepository bindSouvenirRepository(SouvenirRepositoryImpl impl);

    @Binds
    abstract LocationRepository bindLocationRepository(LocationRepositoryImpl impl);

    @Binds
    abstract FirebaseHandler bindFirebaseHandler(MockFirebaseHandlerImpl mockImpl);

    @Binds
    abstract StorageHandler bindStorageHandler(MockStorageHandlerImpl mockImpl);

    @Binds
    abstract DbTaskRunner bindDbTaskRunner(DbTaskRunnerImpl impl);

    @Binds
    abstract LocationTaskRunner bindLocationTaskRunner(LocationTaskRunnerImpl impl);

    @Provides
    static Geocoder provideGeocoder(@AppContext Context context) {
        return new Geocoder(context, Locale.getDefault());
    }

    @Provides
    static FusedLocationProviderClient provideFusedLocationProvider(@AppContext Context context) {
        return new FusedLocationProviderClient(context);
    }

    /**
     * As we cannot access context in the location repo, error messages from string resources are
     * generated on beforehand and injected into the repo.
     */
    @Provides
    @Singleton
    @LocationErrorMsgs
    static Map<Integer,String> provideLocationRepoErrMsgs(@AppContext Context context) {
        Map<Integer,String> locationRepoErrMsgs = new HashMap<>();
        locationRepoErrMsgs.put(R.string.error_message_location_repo_no_device_location,
                context.getString(R.string.error_message_location_repo_no_device_location));
        locationRepoErrMsgs.put(R.string.error_message_location_repo_missing_permission,
                context.getString(R.string.error_message_location_repo_missing_permission));
        locationRepoErrMsgs.put(R.string.error_message_location_repo_no_matching_address,
                context.getString(R.string.error_message_location_repo_no_matching_address));
        locationRepoErrMsgs.put(R.string.error_message_location_repo_error_fetching_location,
                context.getString(R.string.error_message_location_repo_error_fetching_location));
        return locationRepoErrMsgs;
    }

    /**
     * As we cannot access context in the souvenir repo, error messages from string resources are
     * generated on beforehand and injected into the repo.
     */
    @Provides
    @Singleton
    @SouvenirErrorMsgs
    static Map<Integer,String> provideSouvenirRepoErrMsgs(@AppContext Context context) {
        Map<Integer,String> souvenirRepoErrMsgs = new HashMap<>();
        souvenirRepoErrMsgs.put(R.string.error_message_souvenir_repo_not_logged_in,
                context.getString(R.string.error_message_souvenir_repo_not_logged_in));
        souvenirRepoErrMsgs.put(R.string.error_message_souvenir_repo_no_souvenirs_found_on_query,
                context.getString(R.string.error_message_souvenir_repo_no_souvenirs_found_on_query));
        return souvenirRepoErrMsgs;
    }

    /**
     * As we cannot access context in the firebase handler, error messages from string resources are
     * generated on beforehand and injected into the handler.
     */
    @Provides
    @Singleton
    @FirebaseErrorMsgs
    static Map<Integer,String> provideFirebaseErrMsgs(@AppContext Context context) {
        Map<Integer,String> firebaseErrMsgs = new HashMap<>();
        firebaseErrMsgs.put(R.string.error_message_firebase_not_signed_in,
                context.getString(R.string.error_message_firebase_not_signed_in));
        firebaseErrMsgs.put(R.string.error_message_firebase_already_executing,
                context.getString(R.string.error_message_firebase_already_executing));
        return firebaseErrMsgs;
    }

}
