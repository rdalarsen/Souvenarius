package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertTask;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.model.SouvenirResponse;
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
                result.addSource(mSouvenirsOrderByTimeDesc, souvenirDbs -> {
                    result.setValue(Result.success(souvenirDbs));
                });
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
        mSouvenirs.addSource(mFirebaseHandler.getResults(), result -> {
            String resultString = (result.status.equals(Result.Status.SUCCESS))
                    ? result.status.toString()
                    : Result.Status.FAILURE.toString();
            Timber.i("The fetching was a %s", resultString);
        });
        return mSouvenirs;
    }

    public LiveData<List<SouvenirResponse>> getSouvenirs() {
        return Transformations.map(mFirebaseHandler.getResults(), result -> {
            if (Result.Status.SUCCESS.equals(result.status)) {
                return result.response;
            } else if (Result.Status.FAILURE.equals(result.status)) {
                return Collections.emptyList();
            }
            throw new IllegalArgumentException("Unknown status code: " + result.status.name());
        });
    }

    public void addSouvenir(Souvenir souvenir, File image) {
        mStorageHandler.uploadImage(image);
        mFirebaseHandler.storeSouvenir(souvenir);
    }

    public void updateSouvenir(Souvenir souvenir) {
        mFirebaseHandler.storeSouvenir(souvenir);
    }

    public void addNewSouvenir(SouvenirDb db) {
        DataFetchedListener listener = souvenirDbs ->
                Timber.i("souvenirs length from SYNC db call: %d", souvenirDbs.size());

        new SouvenirInsertTask(mAppDatabase.souvenirDao()).execute(db);
    }

    public interface DataFetchedListener {
        void onDataFetched(List<SouvenirDb> souvenirDbs);
    }

}
