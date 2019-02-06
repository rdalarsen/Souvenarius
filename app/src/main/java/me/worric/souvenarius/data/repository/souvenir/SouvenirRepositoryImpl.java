package me.worric.souvenarius.data.repository.souvenir;

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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.tasks.OnResultListener;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.UpdateSouvenirsService;
import me.worric.souvenarius.di.AppContext;
import me.worric.souvenarius.di.SouvenirErrorMsgs;
import me.worric.souvenarius.ui.main.SortStyle;
import timber.log.Timber;

import static me.worric.souvenarius.ui.common.PrefsUtils.PREFS_KEY_SORT_STYLE;
import static me.worric.souvenarius.ui.common.PrefsUtils.getSortStyleFromPrefs;

@Singleton
public class SouvenirRepositoryImpl implements SouvenirRepository {

    private static final String QUERY_STRING = "SELECT * FROM souvenirs WHERE uid = ? ORDER BY timestamp %s";
    private final FirebaseHandler mFirebaseHandler;
    private final StorageHandler mStorageHandler;
    private final Map<Integer,String> mErrorMessages;
    private final FirebaseAuth mAuth;
    private final AppDatabase mAppDatabase;
    private final MutableLiveData<QueryParameters> mQueryParameters = new MutableLiveData<>();
    private final SharedPreferences mPrefs;
    private final DbTaskRunner mDbTaskRunner;

    @Inject
    public SouvenirRepositoryImpl(FirebaseHandler firebaseHandler,
                                  StorageHandler storageHandler,
                                  @SouvenirErrorMsgs Map<Integer,String> errorMessages,
                                  AppDatabase appDatabase,
                                  SharedPreferences prefs,
                                  @AppContext Context context,
                                  DbTaskRunner dbTaskRunner) {
        mFirebaseHandler = firebaseHandler;
        mStorageHandler = storageHandler;
        mErrorMessages = errorMessages;
        mAuth = FirebaseAuth.getInstance();
        mAppDatabase = appDatabase;
        mPrefs = prefs;
        mDbTaskRunner = dbTaskRunner;
        initQueryParameters();
        UpdateSouvenirsService.startSouvenirsUpdate(context);
    }

    private void initQueryParameters() {
        mQueryParameters.setValue(new QueryParameters(mAuth.getUid(), getSortStyleFromPrefs(mPrefs, PREFS_KEY_SORT_STYLE)));
    }

    @Override
    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
        return Transformations.switchMap(mQueryParameters, parameters -> {
            if (TextUtils.isEmpty(parameters.getUid())) {
                return createErrorLiveData();
            }
            return Transformations.map(mAppDatabase.souvenirDao().getSouvenirs(createQuery(parameters)),
                    Result::success);
        });
    }

    private LiveData<Result<List<SouvenirDb>>> createErrorLiveData() {
        MutableLiveData<Result<List<SouvenirDb>>> errorLiveData = new MutableLiveData<>();
        errorLiveData.setValue(Result.failure(mErrorMessages
                .get(R.string.error_message_souvenir_repo_not_logged_in)));
        return errorLiveData;
    }

    private SupportSQLiteQuery createQuery(QueryParameters parameters) {
        String queryString = String.format(QUERY_STRING, parameters.getSortStyle().toString());
        return new SimpleSQLiteQuery(queryString, new Object[]{parameters.getUid()});
    }

    @Override
    public LiveData<SouvenirDb> findSouvenirById(String souvenirId) {
        return mAppDatabase.souvenirDao().findOneById(souvenirId);
    }

    @Override
    public LiveData<Result<List<SouvenirDb>>> findSouvenirsByTitle(String title) {
        return Transformations.map(mAppDatabase.souvenirDao().findSouvenirsByTitle(
                mAuth.getUid(), formatTitleForUseWithLike(title)),
                this::createResult);
    }

    private String formatTitleForUseWithLike(String title) {
        return String.format("%%%s%%", title);
    }

    private Result<List<SouvenirDb>> createResult(List<SouvenirDb> souvenirs) {
        if (souvenirs.isEmpty()) {
            return Result.failure(mErrorMessages.get(R.string.error_message_souvenir_repo_no_souvenirs_found_on_query));
        }
        return Result.success(souvenirs);
    }

    @Override
    public void addSouvenir(SouvenirDb souvenir, File photo, SouvenirRepository.OnAddSuccessListener successListener) {
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

        OnResultListener<SouvenirDb> listener = new OnResultListener<SouvenirDb>() {
            @Override
            public void onSuccess(SouvenirDb souvenirDb) {
                mFirebaseHandler.storeSouvenir(souvenirDb, completionListener);
                if (successListener != null) {
                    successListener.onSuccessfulAdd();
                }
            }

            @Override
            public void onFailure() {
                Timber.e("The database call did not complete successfully");
            }
        };

        souvenir.setTimestamp(Instant.now().toEpochMilli());
        souvenir.setId(UUID.randomUUID().toString());
        souvenir.setUid(mAuth.getUid());

        mDbTaskRunner.runInsertTask(souvenir, mAppDatabase, listener);
    }

    @Override
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

        OnResultListener<SouvenirDb> listener = new OnResultListener<SouvenirDb>() {
            @Override
            public void onSuccess(SouvenirDb souvenirDb) {
                mFirebaseHandler.storeSouvenir(souvenirDb, completionListener);
            }

            @Override
            public void onFailure() {
                Timber.e("The database call did not complete successfully");
            }
        };

        mDbTaskRunner.runUpdateTask(souvenir, mAppDatabase, listener);
    }

    @Override
    public void deleteSouvenir(SouvenirDb souvenir, SouvenirRepository.OnDeleteSuccessListener successListener) {
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

        OnResultListener<SouvenirDb> listener = new OnResultListener<SouvenirDb>() {
            @Override
            public void onSuccess(SouvenirDb souvenirDb) {
                mFirebaseHandler.deleteSouvenir(souvenirDb, completionListener);
                if (successListener != null) {
                    successListener.onSuccessfulDelete();
                }
            }

            @Override
            public void onFailure() {
                Timber.e("The database call did not complete successfully");
            }
        };

        mDbTaskRunner.runDeleteTask(souvenir, mAppDatabase, listener);
    }

    @Override
    public void deletePhotoFromStorage(String photoName) {
        mStorageHandler.removePhoto(photoName);
    }

    @Override
    public void setQueryParameters(String uid) {
        QueryParameters parameters = mQueryParameters.getValue();
        if (parameters != null) {
            parameters.setUid(uid);
            mQueryParameters.setValue(parameters);
        }
    }

    @Override
    public void setQueryParameters(SortStyle sortStyle) {
        QueryParameters parameters = mQueryParameters.getValue();
        if (parameters != null) {
            parameters.setSortStyle(sortStyle);
            mQueryParameters.setValue(parameters);
        }
    }

}
