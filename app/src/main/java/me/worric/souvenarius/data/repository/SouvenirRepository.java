package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import org.threeten.bp.Instant;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.db.tasks.NukeDbTask;
import me.worric.souvenarius.data.db.tasks.SouvenirDeleteTask;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertAllTask;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertTask;
import me.worric.souvenarius.data.db.tasks.SouvenirUpdateTask;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.main.MainActivity;
import me.worric.souvenarius.ui.main.SortStyle;
import timber.log.Timber;

import static me.worric.souvenarius.ui.common.PrefsUtils.getSortStyleFromPrefs;

@Singleton
public class SouvenirRepository {

    public static final String PREFS_KEY_SORT_STYLE = "sortStyle";
    private static final String DEFAULT_QUERY = "SELECT * FROM souvenirs WHERE uid = ? ORDER BY timestamp %s";
    private final FirebaseHandler mFirebaseHandler;
    private final StorageHandler mStorageHandler;
    private final FirebaseAuth mAuth;
    private final AppDatabase mAppDatabase;
    private final MutableLiveData<QueryParameters> mQueryParameters = new MutableLiveData<>();
    //private final MediatorLiveData<Result<List<SouvenirDb>>> mSouvenirs;
    /* see: https://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently */
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangeListener;
    private final SharedPreferences mPrefs;
    private Boolean mIsConnected;

    @Inject
    public SouvenirRepository(FirebaseHandler firebaseHandler,
                              StorageHandler storageHandler,
                              AppDatabase appDatabase,
                              SharedPreferences prefs,
                              @AppContext Context context) {
        mFirebaseHandler = firebaseHandler;
        mStorageHandler = storageHandler;
        mAuth = FirebaseAuth.getInstance();
        mAppDatabase = appDatabase;
        mPrefs = prefs;
        mPrefsChangeListener = initPrefListener(prefs);
        setupQueryParameters();
        //mSouvenirs = initSouvenirs(prefs);
        subscribeToRemoteDatabaseUpdates();
        initConnectionStateDetection(context);
        initAuthStateDetection(context);
    }

    private void setupQueryParameters() {
        mQueryParameters.setValue(new QueryParameters(mAuth.getUid(), getSortStyleFromPrefs(mPrefs, PREFS_KEY_SORT_STYLE)));
    }

    private void initAuthStateDetection(Context context) {
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_AUTH_SIGNED_OUT);
        filter.addAction(MainActivity.ACTION_AUTH_SIGNED_IN);
        LocalBroadcastManager.getInstance(context).registerReceiver(mAuthStateChangedReceiver, filter);
    }

    private void initConnectionStateDetection(Context context) {
        mIsConnected = NetUtils.getIsConnected(context);
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_CONNECTIVITY_CHANGED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mConnectionStateReceiver, filter);
    }

    private BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra(MainActivity.KEY_IS_CONNECTED, false);
            Timber.i("Received connection broadcast. we are connected=%s", isConnected);
            boolean needsUpdate = !(mIsConnected == isConnected);
            if (needsUpdate) {
                mIsConnected = isConnected;
                Timber.i("new connected status triggered");
            }
        }
    };

    private BroadcastReceiver mAuthStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.i("auth state changed received! action was: %s", intent.getAction());
        }
    };

    private void subscribeToRemoteDatabaseUpdates() {
        mFirebaseHandler.getResults().observeForever(result -> {
            Timber.d("Firebase database update triggered");
            if (result.status.equals(Result.Status.SUCCESS)) {
                SouvenirDb[] converted = result.response.toArray(new SouvenirDb[]{});
                new SouvenirInsertAllTask(mAppDatabase).execute(converted);
            }
        });
    }

    public void fetchNewSouvenirs() {
        mFirebaseHandler.fetchSouvenirs();
    }

    private MediatorLiveData<Result<List<SouvenirDb>>> initSouvenirs(SharedPreferences prefs) {
        MediatorLiveData<Result<List<SouvenirDb>>> result = new MediatorLiveData<>();
        SortStyle initialSortStyle = getSortStyleFromPrefs(prefs, PREFS_KEY_SORT_STYLE);
        setSouvenirSource(initialSortStyle, result);
        return result;
    }

    private void setSouvenirSource(SortStyle sortStyle, MediatorLiveData<Result<List<SouvenirDb>>> result) {
        /*switch (sortStyle) {
            case DESC:
                result.removeSource(mSouvenirsOrderByTimeAsc);
                result.addSource(mSouvenirsOrderByTimeDesc, souvenirDbs ->
                        result.setValue(Result.success(souvenirDbs)));
                break;
            case ASC:
                result.removeSource(mSouvenirsOrderByTimeDesc);
                result.addSource(mSouvenirsOrderByTimeAsc, souvenirDbs ->
                        result.setValue(Result.success(souvenirDbs)));
                break;
            default:
                throw new IllegalArgumentException("Unknown SortStyle: " + sortStyle.toString());
        }*/
    }

    private SharedPreferences.OnSharedPreferenceChangeListener initPrefListener(SharedPreferences prefs) {
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
            SortStyle sortStyle = getSortStyleFromPrefs(sharedPreferences, key);
            Timber.i("SortStyle is now set to: %s", sortStyle.toString());
            //setSouvenirSource(sortStyle, mSouvenirs);
            mQueryParameters.setValue(new QueryParameters(mAuth.getUid(), sortStyle));
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        return listener;
    }

    public LiveData<Result<List<SouvenirDb>>> getSortedSouvenirs() {
        // Set logged-in user
        return Transformations.switchMap(mQueryParameters, theQuery -> {
            if (TextUtils.isEmpty(theQuery.uid)) {
                MutableLiveData<Result<List<SouvenirDb>>> errorLiveData = new MutableLiveData<>();
                errorLiveData.setValue(Result.failure("Not logged in."));
                return errorLiveData;
            }
            String queryString = String.format(DEFAULT_QUERY, theQuery.sortStyle.toString());
            SupportSQLiteQuery simpleQuery = new SimpleSQLiteQuery(queryString, new Object[]{theQuery.uid});
            return Transformations.map(mAppDatabase.souvenirDao().getSouvenirs(simpleQuery),
                    Result::success);
        });
    }

    public void addNewSouvenir(SouvenirDb db, File photo) {
        //TODO: make other DB interactions use appropriate completionlisteners
        DatabaseReference.CompletionListener completionListener = (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Timber.e(databaseError.toException(), "There was a problem uploading the data to the database; not uploading photo to FirebaseStorage");
                return;
            }
            if (photo != null) {
                mStorageHandler.uploadImage(photo);
            } else {
                Timber.e("The photo was null; not uploading photo to FirebaseStorage");
            }
        };

        DataInsertCallback callback = souvenirDb -> {
            if (souvenirDb != null) {
                Timber.i("souvenir is NOT null! The ID is: %s", souvenirDb.getId());
                mFirebaseHandler.storeSouvenir(souvenirDb, completionListener);
            }
        };

        db.setTimestamp(Instant.now().toEpochMilli());
        db.setId(UUID.randomUUID().toString());
        db.setUID(mAuth.getCurrentUser().getUid());
        new SouvenirInsertTask(mAppDatabase.souvenirDao(), callback).execute(db);
    }

    public LiveData<SouvenirDb> findOneById(String souvenirId) {
        return mAppDatabase.souvenirDao().findOneById(souvenirId);
    }

    public SouvenirDb findMostRecentSouvenir() {
        return mAppDatabase.souvenirDao().findMostRecentSync();
    }

    public void updateSouvenir(SouvenirDb souvenir, File photo) {
        DatabaseReference.CompletionListener completionListener = (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Timber.e(databaseError.toException(), "There was a problem uploading the data to the database");
                return;
            }
            if (photo != null) {
                Timber.i("attempting to upload the photo...");
                mStorageHandler.uploadImage(photo);
            } else {
                Timber.e("The photo was null!");
            }
        };

        DataUpdateCallback callback = numRowsAffected -> {
            if (numRowsAffected > 0) {
                Timber.i("data was updated just fine!");
                mFirebaseHandler.addSouvenir(souvenir, completionListener);
            }
        };

        new SouvenirUpdateTask(mAppDatabase.souvenirDao(), callback).execute(souvenir);
    }

    public void deleteFileFromStorage(String photoName) {
        mStorageHandler.removeImage(photoName);
    }

    public void nukeDb() {
        new NukeDbTask(mAppDatabase.souvenirDao()).execute();
    }

    public void deleteSouvenir(SouvenirDb souvenir) {
        DataDeletedCallback callback = numRowsAffected -> {
            if (numRowsAffected > 0) {
                mFirebaseHandler.deleteSouvenir(souvenir);
                mStorageHandler.removeImages(souvenir.getPhotos());
            }
        };
        new SouvenirDeleteTask(mAppDatabase.souvenirDao(), callback).execute(souvenir);
    }

    public interface DataInsertCallback {
        void onDataInserted(SouvenirDb souvenirDb);
    }

    public interface DataUpdateCallback {
        void onDataUpdated(int numRowsAffected);
    }

    public interface DataDeletedCallback {
        void onDataDeleted(int numRowsAffected);
    }

    static class QueryParameters {

        final String uid;
        final SortStyle sortStyle;

        public QueryParameters(@Nullable String uid, @NonNull SortStyle sortStyle) {
            this.uid = uid;
            this.sortStyle = sortStyle;
        }
    }

}
