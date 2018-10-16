package me.worric.souvenarius.data.repository;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import me.worric.souvenarius.BuildConfig;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.tasks.SouvenirInsertAllTask;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.FirebaseHandler;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;
import timber.log.Timber;

/**
 * JobIntentService is a compatibility class inheriting from IntentService, that enables correct
 * background processing on pre and post Oreo devices. That is, use JobScheduler on Oreo and later,
 * and start IntentService normally on pre Oreo
 *
 * See <a href="https://developer.android.com/reference/android/support/v4/app/JobIntentService"></a>.
 */
public class UpdateSouvenirsService extends JobIntentService {

    private static final String ACTION_UPDATE_SOUVENIRS = "action_update_souvenirs";
    private static final int JOB_ID = 543;

    private FirebaseAuth mAuth;
    @Inject
    protected FirebaseHandler mFirebaseHandler;
    @Inject
    protected AppDatabase mAppDatabase;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
    }

    public static void startSouvenirsUpdate(Context context) {
        Intent intent = new Intent(context, UpdateSouvenirsService.class);
        intent.setAction(ACTION_UPDATE_SOUVENIRS);
        enqueueWork(context, UpdateSouvenirsService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) throw new IllegalArgumentException("Null or empty action");
        switch (action) {
            case ACTION_UPDATE_SOUVENIRS:
                if (TextUtils.isEmpty(mAuth.getUid())) {
                    Timber.i("user us not logged in. Not running update.");
                    break;
                }
                handleUpdateSouvenirs();
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }

    }

    private void handleUpdateSouvenirs() {
        mFirebaseHandler.fetchSouvenirsForCurrentUser(result -> {
            if (result.status.equals(Result.Status.SUCCESS)) {
                SouvenirDb[] converted = result.response.toArray(new SouvenirDb[]{});
                /* This is a workaround to make sure we can update the db off the main thread */
                new SouvenirInsertAllTask(mAppDatabase, mAuth.getUid(), mListener).execute(converted);
            } else {
                Timber.w("The fetching failed. Message is: %s", result.message);
            }
            Timber.i("update of souvenirs done");
        });
    }

    private SouvenirInsertAllTask.OnDataInsertAllListener mListener = () -> {
        /* Update widget so as to make sure we display the most current data */
        UpdateWidgetService.startWidgetUpdate(this);
    };

}
