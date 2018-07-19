package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertTask;
import me.worric.souvenarius.data.db.tasks.SouvenirUpdateTask;
import me.worric.souvenarius.ui.main.SortStyle;
import timber.log.Timber;

@Singleton
public class SouvenirRepository {

    public static final String PREFS_KEY_SORT_STYLE = "sortStyle";
    private final FirebaseHandler mFirebaseHandler;
    private final StorageHandler mStorageHandler;
    private final AppDatabase mAppDatabase;
    private final LiveData<List<SouvenirDb>> mSouvenirsOrderByTimeAsc;
    private final LiveData<List<SouvenirDb>> mSouvenirsOrderByTimeDesc;
    private final MediatorLiveData<Result<List<SouvenirDb>>> mSouvenirs;
    /* see: https://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently */
    private final SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;

    @Inject
    public SouvenirRepository(FirebaseHandler firebaseHandler,
                              StorageHandler storageHandler,
                              AppDatabase appDatabase,
                              SharedPreferences prefs) {
        mFirebaseHandler = firebaseHandler;
        mStorageHandler = storageHandler;
        mAppDatabase = appDatabase;
        mSouvenirsOrderByTimeAsc = mAppDatabase.souvenirDao().findAllOrderByTimeAsc();
        mSouvenirsOrderByTimeDesc = mAppDatabase.souvenirDao().findAllOrderByTimeDesc();
        mSouvenirs = initSouvenirs(prefs);
        mPreferenceChangeListener = initPrefListener(prefs);
    }

    private MediatorLiveData<Result<List<SouvenirDb>>> initSouvenirs(SharedPreferences prefs) {
        MediatorLiveData<Result<List<SouvenirDb>>> result = new MediatorLiveData<>();
        SortStyle initialSortStyle = getSortStyleFromPrefs(prefs, PREFS_KEY_SORT_STYLE);
        setSouvenirSource(initialSortStyle, result);
        return result;
    }

    private void setSouvenirSource(SortStyle sortStyle, MediatorLiveData<Result<List<SouvenirDb>>> result) {
        switch (sortStyle) {
            case DATE_DESC:
                result.removeSource(mSouvenirsOrderByTimeAsc);
                result.addSource(mSouvenirsOrderByTimeDesc, souvenirDbs ->
                        result.setValue(Result.success(souvenirDbs)));
                break;
            case DATE_ASC:
                result.removeSource(mSouvenirsOrderByTimeDesc);
                result.addSource(mSouvenirsOrderByTimeAsc, souvenirDbs ->
                        result.setValue(Result.success(souvenirDbs)));
                break;
            default:
                throw new IllegalArgumentException("Unknown SortStyle: " + sortStyle.toString());
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener initPrefListener(SharedPreferences prefs) {
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
            SortStyle sortStyle = getSortStyleFromPrefs(sharedPreferences, key);
            Timber.i("SortStyle is now set to: %s", sortStyle.toString());
            setSouvenirSource(sortStyle, mSouvenirs);
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        return listener;
    }

    private SortStyle getSortStyleFromPrefs(SharedPreferences sharedPreferences, String key) {
        Timber.i("getting sortStyle from prefs. Key is: %s, and value is: %s", key, sharedPreferences.getString(key, "defValue"));
        String value = sharedPreferences.getString(key, SortStyle.DATE_DESC.toString());
        return SortStyle.valueOf(value);
    }

    public LiveData<Result<List<SouvenirDb>>> getSortedSouvenirs() {
        fetchDataFromFirebase();
        return mSouvenirs;
    }

    private void fetchDataFromFirebase() {
        mSouvenirs.addSource(mFirebaseHandler.getResults(), result -> {
            String resultString = (result.status.equals(Result.Status.SUCCESS))
                    ? result.status.toString()
                    : Result.Status.FAILURE.toString();
            Timber.i("The fetching was a %s", resultString);
            if (result.status.equals(Result.Status.SUCCESS)) {

            }
        });
    }

    public LiveData<List<SouvenirDb>> getSouvenirs() {
        return Transformations.map(mFirebaseHandler.getResults(), result -> {
            if (Result.Status.SUCCESS.equals(result.status)) {
                return result.response;
            } else if (Result.Status.FAILURE.equals(result.status)) {
                return Collections.emptyList();
            }
            throw new IllegalArgumentException("Unknown status code: " + result.status.name());
        });
    }

    public void addNewSouvenir(SouvenirDb db, File photo) {
        DatabaseReference.CompletionListener completionListener = (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Timber.e(databaseError.toException(),"There was a problem uploading the data to the database");
                return;
            }
            if (photo != null) {
                mStorageHandler.uploadImage(photo);
            } else {
                Timber.e("The photo was null!");
            }
        };

        DataInsertCallback callback = souvenirDb -> {
            if (souvenirDb != null) {
                Timber.i("souvenir is NOT null! The ID is: %d", souvenirDb.getId());
                mFirebaseHandler.storeSouvenir(souvenirDb, completionListener);
            }
        };

        new SouvenirInsertTask(mAppDatabase.souvenirDao(), callback).execute(db);
    }

    public LiveData<SouvenirDb> findOne(long souvenirId) {
        return mAppDatabase.souvenirDao().findOneById(souvenirId);
    }

    public void updateSouvenir(SouvenirDb souvenir) {
        DataUpdateCallback callback = numRowsAffected -> {
            if (numRowsAffected > 0) Timber.i("data was updated just fine!");
        };
        new SouvenirUpdateTask(mAppDatabase.souvenirDao(), callback).execute(souvenir);
    }

    public interface DataInsertCallback {
        void onDataInserted(SouvenirDb souvenirDb);
    }

    public interface DataUpdateCallback {
        void onDataUpdated(int numRowsAffected);
    }

    private LiveData<SouvenirDb> mGetSouvenirDbFromResults =
            Transformations.switchMap(getInsertId(), id ->
                    Transformations.map(getSortedSouvenirs(), result -> {
                        if (result.status.equals(Result.Status.SUCCESS)) {
                            for (SouvenirDb souvenirDb : result.response) {
                                if (souvenirDb.getId() == id) {
                                    return souvenirDb;
                                }
                            }
                        }
                        return null;
                    }));

    private MutableLiveData<Long> mInsertedId = new MutableLiveData<>();

    private LiveData<Long> getInsertId() {
        return mInsertedId;
    }

}
