package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
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
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.db.tasks.NukeDbTask;
import me.worric.souvenarius.data.db.tasks.SouvenirDeleteTask;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertAllTask;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertTask;
import me.worric.souvenarius.data.db.tasks.SouvenirUpdateTask;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.di.SouvenirErrorMsgs;
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.main.MainActivity;
import me.worric.souvenarius.ui.main.SortStyle;
import timber.log.Timber;

import static me.worric.souvenarius.ui.common.PrefsUtils.getSortStyleFromPrefs;

@Singleton
public class SouvenirRepository {

    public static final String PREFS_KEY_SORT_STYLE = "sortStyle";
    private static final String QUERY_STRING = "SELECT * FROM souvenirs WHERE uid = ? ORDER BY timestamp %s";
    private final FirebaseHandler mFirebaseHandler;
    private final StorageHandler mStorageHandler;
    private final Map<Integer,String> mErrorMessages;
    private final FirebaseAuth mAuth;
    private final AppDatabase mAppDatabase;
    private final MutableLiveData<QueryParameters> mQueryParameters = new MutableLiveData<>();
    /* see: https://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently */
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangeListener;
    private final SharedPreferences mPrefs;
    private Boolean mIsConnected;

    @Inject
    public SouvenirRepository(FirebaseHandler firebaseHandler,
                              StorageHandler storageHandler,
                              @SouvenirErrorMsgs Map<Integer,String> errorMessages,
                              AppDatabase appDatabase,
                              SharedPreferences prefs,
                              @AppContext Context context) {
        mFirebaseHandler = firebaseHandler;
        mStorageHandler = storageHandler;
        mErrorMessages = errorMessages;
        mAuth = FirebaseAuth.getInstance();
        mAppDatabase = appDatabase;
        mPrefs = prefs;
        mPrefsChangeListener = initPrefListener(prefs);
        setupQueryParameters();
        subscribeToRemoteDatabaseUpdates();
        initConnectionStateDetection(context);
        initAuthStateDetection(context);
        refreshSouvenirsFromRemote();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener initPrefListener(SharedPreferences prefs) {
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
            SortStyle sortStyle = getSortStyleFromPrefs(sharedPreferences, key);
            Timber.i("SortStyle is now set to: %s", sortStyle.toString());
            mQueryParameters.setValue(new QueryParameters(mAuth.getUid(), sortStyle));
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        return listener;
    }

    private void setupQueryParameters() {
        mQueryParameters.setValue(new QueryParameters(mAuth.getUid(), getSortStyleFromPrefs(mPrefs, PREFS_KEY_SORT_STYLE)));
    }

    private void subscribeToRemoteDatabaseUpdates() {
        mFirebaseHandler.getResults().observeForever(result -> {
            Timber.d("Firebase database update triggered");
            if (result.status.equals(Result.Status.SUCCESS)) {
                SouvenirDb[] converted = result.response.toArray(new SouvenirDb[]{});
                new SouvenirInsertAllTask(mAppDatabase).execute(converted);
            }
        });
    }

    private void initConnectionStateDetection(Context context) {
        mIsConnected = NetUtils.getIsConnected(context);
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_CONNECTIVITY_CHANGED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mConnectionStateReceiver, filter);
    }

    private void initAuthStateDetection(Context context) {
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_AUTH_SIGNED_OUT);
        filter.addAction(MainActivity.ACTION_AUTH_SIGNED_IN);
        LocalBroadcastManager.getInstance(context).registerReceiver(mAuthStateChangedReceiver, filter);
    }

    public void refreshSouvenirsFromRemote() {
        mFirebaseHandler.fetchSouvenirsForCurrentUser();
    }

    public LiveData<Result<List<SouvenirDb>>> getSortedSouvenirs() {
        return Transformations.switchMap(mQueryParameters, parameters -> {
            if (TextUtils.isEmpty(parameters.getUid())) {
                MutableLiveData<Result<List<SouvenirDb>>> errorLiveData = new MutableLiveData<>();
                errorLiveData.setValue(Result.failure(mErrorMessages
                        .get(R.string.error_message_souvenir_repo_not_logged_in)));
                return errorLiveData;
            }
            String queryString = String.format(QUERY_STRING, parameters.getSortStyle().toString());
            SupportSQLiteQuery simpleQuery = new SimpleSQLiteQuery(queryString, new Object[]{parameters.getUid()});
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
            if (photo != null && photo.exists()) {
                mStorageHandler.uploadImage(photo);
            } else {
                Timber.e("The photo was null or did not exist; not uploading...");
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
        db.setUID(mAuth.getUid());
        new SouvenirInsertTask(mAppDatabase.souvenirDao(), callback).execute(db);
    }

    public LiveData<SouvenirDb> findOneById(String souvenirId) {
        return mAppDatabase.souvenirDao().findOneById(souvenirId);
    }

    public void updateSouvenir(SouvenirDb souvenir, File photo) {
        DatabaseReference.CompletionListener completionListener = (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Timber.e(databaseError.toException(), "There was a problem uploading the data to the database");
                return;
            }
            if (photo != null && photo.exists()) {
                Timber.i("attempting to upload the photo...");
                mStorageHandler.uploadImage(photo);
            } else {
                Timber.e("The photo was null or did not exist; not uploading...");
            }
        };

        DataUpdateCallback callback = numRowsAffected -> {
            if (numRowsAffected > 0) {
                Timber.i("data was updated just fine!");
                mFirebaseHandler.storeSouvenir(souvenir, completionListener);
            }
        };

        new SouvenirUpdateTask(mAppDatabase.souvenirDao(), callback).execute(souvenir);
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

    public SouvenirDb findMostRecentSouvenir(String uid) {
        return mAppDatabase.souvenirDao().findMostRecentSync(uid);
    }

    public void deleteFileFromStorage(String photoName) {
        mStorageHandler.removeImage(photoName);
    }

    public void nukeDb() {
        new NukeDbTask(mAppDatabase.souvenirDao()).execute();
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
            if (intent.getAction().equals(MainActivity.ACTION_AUTH_SIGNED_IN)) {
                refreshSouvenirsFromRemote();
            } else if (intent.getAction().equals(MainActivity.ACTION_AUTH_SIGNED_OUT)) {
                mFirebaseHandler.clearResults();
            }
            setQueryParameters(mAuth.getUid());
        }
    };

    private void setQueryParameters(String uid) {
        QueryParameters parameters = mQueryParameters.getValue();
        if (parameters != null) {
            parameters.setUid(uid);
            mQueryParameters.setValue(parameters);
        }
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

        private String uid;
        private SortStyle sortStyle;

        QueryParameters(@Nullable String uid, @NonNull SortStyle sortStyle) {
            this.uid = uid;
            this.sortStyle = sortStyle;
        }

        String getUid() {
            return uid;
        }

        void setUid(String uid) {
            this.uid = uid;
        }

        SortStyle getSortStyle() {
            return sortStyle;
        }

        public void setSortStyle(SortStyle sortStyle) {
            this.sortStyle = sortStyle;
        }

    }

}
