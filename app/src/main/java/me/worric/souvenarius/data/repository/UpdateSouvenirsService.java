package me.worric.souvenarius.data.repository;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import dagger.android.AndroidInjection;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertAllTask;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.FirebaseHandler;
import me.worric.souvenarius.ui.authwrapper.AppAuth;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;
import timber.log.Timber;

/**
 * JobIntentService is a compatibility class inheriting from IntentService, that enables correct
 * background processing on pre and post Oreo devices. That is, use JobScheduler on Oreo and later,
 * and start IntentService normally on pre Oreo
 *
 * See <a href="https://developer.android.com/reference/android/support/v4/app/JobIntentService">this documentation</a>.
 */
public class UpdateSouvenirsService extends JobIntentService {

    private static final String ACTION_UPDATE_SOUVENIRS = "action_update_souvenirs";
    private static final int JOB_ID = 543;
    @Inject AppAuth mAppAuth;
    @Inject AppDatabase mAppDatabase;
    @Inject FirebaseHandler mFirebaseHandler;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    public static void startSouvenirsUpdate(Context context) {
        Timber.i("UpdateSouvenirsService started");
        Intent intent = new Intent(context, UpdateSouvenirsService.class);
        intent.setAction(ACTION_UPDATE_SOUVENIRS);
        enqueueWork(context, UpdateSouvenirsService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) throw new IllegalArgumentException("Null or empty action");
        switch (action) {
            case ACTION_UPDATE_SOUVENIRS:
                if (TextUtils.isEmpty(mAppAuth.getUid())) {
                    Timber.i("No user logged in. Not running update.");
                    break;
                }
                handleUpdateSouvenirs();
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }

    }

    private void handleUpdateSouvenirs() {
        Timber.i("Handling souvenirs update");
        mFirebaseHandler.fetchSouvenirsForCurrentUser(result -> {
            if (result.status.equals(Result.Status.SUCCESS)) {
                SouvenirDb[] converted = result.response.toArray(new SouvenirDb[]{});
                /* This is a workaround to make sure we can update the db off the main thread */
                new SouvenirInsertAllTask(mAppDatabase, mAppAuth.getUid(), mListener).execute(converted);
            } else {
                Timber.w("The fetching failed. Message is: %s", result.message);
            }
            Timber.i("Update of souvenirs done");
        });
    }

    private SouvenirInsertAllTask.OnDataInsertAllListener mListener = () -> {
        /* Update widget so as to make sure we display the most current data */
        UpdateWidgetService.startWidgetUpdate(this);
    };

}
