package me.worric.souvenarius.ui.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import dagger.android.AndroidInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.ui.authwrapper.AppAuth;
import timber.log.Timber;

/**
 * JobIntentService is a compatibility class inheriting from IntentService, that enables correct
 * background processing on pre and post Oreo devices. That is, use JobScheduler on Oreo and later,
 * and start IntentService normally on pre Oreo
 *
 * See <a href="https://developer.android.com/reference/android/support/v4/app/JobIntentService"></a>.
 */
public class UpdateWidgetService extends JobIntentService {

    private static final String ACTION_UPDATE_WIDGET = "action_update_widget";
    private static final int JOB_ID = 345;
    @Inject AppAuth mAppAuth;
    @Inject AppDatabase mAppDatabase;
    @Inject Handler mMainThreadHandler;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    public static void startWidgetUpdate(Context context) {
        Timber.i("UpdateWidgetService started");
        Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        enqueueWork(context, UpdateWidgetService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) throw new IllegalArgumentException("Null or empty action");
        switch (action) {
            case ACTION_UPDATE_WIDGET:
                handleUpdateWidget();
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private void handleUpdateWidget() {
        Timber.i("Handling widget update");

        Result<SouvenirDb> resultSouvenir;
        String currentUserUid = mAppAuth.getUid();

        if (!TextUtils.isEmpty(currentUserUid)) {
            SouvenirDb souvenir = mAppDatabase.souvenirDao().findMostRecentSync(currentUserUid);

            if (souvenir == null) {
                resultSouvenir = Result.failure(getString(R.string.error_message_widget_no_souvenirs));
            } else {
                resultSouvenir = Result.success(souvenir);
            }
        } else {
            resultSouvenir = Result.failure(getString(R.string.error_message_widget_not_signed_in));
        }

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(getApplicationContext(),
                SouvenirWidgetProvider.class));

        /* The actual update must run on the main thread, else Glide won't work */
        mMainThreadHandler.post(() ->
                SouvenirWidgetProvider.updateAppWidgets(this, manager, widgetIds, resultSouvenir));
    }

}
