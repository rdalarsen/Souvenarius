package me.worric.souvenarius;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import dagger.multibindings.IntoMap;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.di.FirebaseErrorMsgs;
import me.worric.souvenarius.di.KeyForViewModel;
import me.worric.souvenarius.di.LocationErrorMsgs;
import me.worric.souvenarius.di.SouvenirErrorMsgs;
import me.worric.souvenarius.ui.add.AddViewModel;
import me.worric.souvenarius.ui.common.AppViewModelFactory;
import me.worric.souvenarius.ui.common.PrefsUtils;
import me.worric.souvenarius.ui.detail.DetailViewModel;
import me.worric.souvenarius.ui.main.MainActivity;
import me.worric.souvenarius.ui.main.MainActivityModule;
import me.worric.souvenarius.ui.main.MainViewModel;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;

@Module(includes = AndroidSupportInjectionModule.class)
public abstract class SouvenirAppModule {

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
    @KeyForViewModel(MainViewModel.class)
    abstract ViewModel bindViewModel(MainViewModel mainViewModel);

    @Binds
    @IntoMap
    @KeyForViewModel(DetailViewModel.class)
    abstract ViewModel bindDetailViewModel(DetailViewModel detailViewModel);

    @Binds
    @IntoMap
    @KeyForViewModel(AddViewModel.class)
    abstract ViewModel bindAddViewModel(AddViewModel addViewModel);

    @Provides
    @Singleton
    static AppDatabase provideDatabase(@AppContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, AppDatabase.DB_NAME).build();
    }

    @Provides
    @Singleton
    static SharedPreferences provideSharedPreferences(@AppContext Context context) {
        return context.getSharedPreferences(PrefsUtils.PREFS_NAME, Context.MODE_PRIVATE);
    }

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

    @Provides
    @Singleton
    @SouvenirErrorMsgs
    static Map<Integer,String> provideSouvenirRepoErrMsgs(@AppContext Context context) {
        Map<Integer,String> souvenirRepoErrMsgs = new HashMap<>();
        souvenirRepoErrMsgs.put(R.string.error_message_souvenir_repo_not_logged_in,
                context.getString(R.string.error_message_souvenir_repo_not_logged_in));
        return souvenirRepoErrMsgs;
    }

    @Provides
    @Singleton
    @FirebaseErrorMsgs
    static Map<Integer,String> provideFirebaseErrMsgs(@AppContext Context context) {
        Map<Integer,String> firebaseErrMsgs = new HashMap<>();
        firebaseErrMsgs.put(R.string.error_message_firebase_result_cleared,
                context.getString(R.string.error_message_firebase_result_cleared));
        return firebaseErrMsgs;
    }

    @Provides
    static Geocoder provideGeocoder(@AppContext Context context) {
        return new Geocoder(context, Locale.getDefault());
    }

    @Provides
    static FusedLocationProviderClient provideFusedLocationProvider(@AppContext Context context) {
        return new FusedLocationProviderClient(context);
    }

}
