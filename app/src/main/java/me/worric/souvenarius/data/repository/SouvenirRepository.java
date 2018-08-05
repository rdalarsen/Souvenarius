package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Context;
import android.content.SharedPreferences;
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
import me.worric.souvenarius.data.db.tasks.SouvenirDeleteTask;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertTask;
import me.worric.souvenarius.data.db.tasks.SouvenirUpdateTask;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.di.SouvenirErrorMsgs;
import me.worric.souvenarius.ui.main.SortStyle;
import timber.log.Timber;

import static me.worric.souvenarius.ui.common.PrefsUtils.PREFS_KEY_SORT_STYLE;
import static me.worric.souvenarius.ui.common.PrefsUtils.getSortStyleFromPrefs;

@Singleton
public class SouvenirRepository {

    private static final String QUERY_STRING = "SELECT * FROM souvenirs WHERE uid = ? ORDER BY timestamp %s";
    private final FirebaseHandler mFirebaseHandler;
    private final StorageHandler mStorageHandler;
    private final Map<Integer,String> mErrorMessages;
    private final FirebaseAuth mAuth;
    private final AppDatabase mAppDatabase;
    private final MutableLiveData<QueryParameters> mQueryParameters = new MutableLiveData<>();
    private final SharedPreferences mPrefs;

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
        initQueryParameters();
        UpdateSouvenirsService.startSouvenirsUpdate(context);
    }

    private void initQueryParameters() {
        mQueryParameters.setValue(new QueryParameters(mAuth.getUid(), getSortStyleFromPrefs(mPrefs, PREFS_KEY_SORT_STYLE)));
    }

    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
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

    public LiveData<SouvenirDb> findSouvenirById(String souvenirId) {
        return mAppDatabase.souvenirDao().findOneById(souvenirId);
    }

    public void addSouvenir(SouvenirDb db, File photo) {
        DatabaseReference.CompletionListener completionListener = (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Timber.e(databaseError.toException(), "There was a problem adding the souvenir to the database.");
                return;
            }

            if (photo != null && photo.exists()) {
                mStorageHandler.uploadPhoto(photo);
            } else {
                Timber.e("The photo was null or did not exist; not uploading...");
            }
        };

        SouvenirInsertTask.OnDataInsertListener listener = souvenirDb -> {
            if (souvenirDb != null) {
                mFirebaseHandler.storeSouvenir(souvenirDb, completionListener);
            }
        };

        db.setTimestamp(Instant.now().toEpochMilli());
        db.setId(UUID.randomUUID().toString());
        db.setUid(mAuth.getUid());

        new SouvenirInsertTask(mAppDatabase.souvenirDao(), listener).execute(db);
    }

    public void updateSouvenir(SouvenirDb souvenir, File photo) {
        DatabaseReference.CompletionListener completionListener = (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Timber.e(databaseError.toException(), "There was a problem updating the souvenir in the database.");
                return;
            }

            if (photo != null && photo.exists()) {
                mStorageHandler.uploadPhoto(photo);
            } else {
                Timber.e("The photo was null or did not exist; not uploading...");
            }
        };

        SouvenirUpdateTask.OnDataUpdateListener listener = numRowsAffected -> {
            if (numRowsAffected > 0) {
                mFirebaseHandler.storeSouvenir(souvenir, completionListener);
            }
        };

        new SouvenirUpdateTask(mAppDatabase.souvenirDao(), listener).execute(souvenir);
    }

    public void deleteSouvenir(SouvenirDb souvenir) {
        DatabaseReference.CompletionListener completionListener = (databaseError, databaseReference) -> {
            if (databaseError != null) {
                Timber.e(databaseError.toException(), "There was a problem deleting the souvenir from the database.");
                return;
            }

            List<String> photos = souvenir.getPhotos();
            if (!photos.isEmpty()) {
                mStorageHandler.removePhotos(photos);
            } else {
                Timber.e("The list of photos was empty; not deleting from storage.");
            }
        };

        SouvenirDeleteTask.OnDataDeleteListener listener = numRowsAffected -> {
            if (numRowsAffected > 0) {
                mFirebaseHandler.deleteSouvenir(souvenir, completionListener);
            }
        };

        new SouvenirDeleteTask(mAppDatabase.souvenirDao(), listener).execute(souvenir);
    }

    public void deletePhotoFromStorage(String photoName) {
        mStorageHandler.removePhoto(photoName);
    }

    public void setQueryParameters(String uid) {
        QueryParameters parameters = mQueryParameters.getValue();
        if (parameters != null) {
            parameters.setUid(uid);
            mQueryParameters.setValue(parameters);
        }
    }

    public void setQueryParameters(SortStyle sortStyle) {
        QueryParameters parameters = mQueryParameters.getValue();
        if (parameters != null) {
            parameters.setSortStyle(sortStyle);
            mQueryParameters.setValue(parameters);
        }
    }

}
