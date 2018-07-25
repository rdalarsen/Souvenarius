package me.worric.souvenarius.ui.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class UpdateWidgetService extends JobIntentService {

    private static final String UPDATE_WIDGET = "update_widget";
    private static final int JOB_ID = 345;
    private Handler mHandler;
    private FirebaseAuth mAuth;
    @Inject
    protected SouvenirRepository mRepository;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mAuth = FirebaseAuth.getInstance();
    }

    public static void startWidgetUpdate(Context context) {
        Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.setAction(UPDATE_WIDGET);
        enqueueWork(context, UpdateWidgetService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) throw new IllegalArgumentException("Null or empty action");
        switch (action) {
            case UPDATE_WIDGET:
                handleUpdateWidget();
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private void handleUpdateWidget() {
        Timber.i("handleUpdateWidget triggered");

        Result<SouvenirDb> resultSouvenir;

        if (mAuth.getCurrentUser() != null) {
            SouvenirDb souvenir = mRepository.findMostRecentSouvenir();

            if (souvenir == null) {
                resultSouvenir = Result.failure(getString(R.string.error_message_widget_no_souvenirs));
            } else {
                resultSouvenir = Result.success(souvenir);
            }
        } else {
            resultSouvenir = Result.failure(getString(R.string.error_message_widget_not_signed_in));
        }


        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(this, SouvenirWidgetProvider.class));

        /* The actual update must run on the main thread, else Glide won't work */
        mHandler.post(() ->
                SouvenirWidgetProvider.updateAppWidgets(this, manager, widgetIds, resultSouvenir));
    }

}
