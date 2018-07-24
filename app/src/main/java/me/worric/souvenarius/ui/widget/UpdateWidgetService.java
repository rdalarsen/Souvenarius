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

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class UpdateWidgetService extends JobIntentService {

    private static final String UPDATE_WIDGET = "update_widget";
    private static final int JOB_ID = 345;
    private Handler mHandler;
    @Inject
    protected SouvenirRepository mRepository;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
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
        List<SouvenirDb> souvenirs = mRepository.findAllOrderByTimeDescSync();
        SouvenirDb firstSouvenir = souvenirs != null && !souvenirs.isEmpty() ? souvenirs.get(0) : null;
        Timber.i("fetched results from the db: %s", souvenirs != null ? souvenirs.toString() : "souvenirs is null!");

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(this, SouvenirWidgetProvider.class));

        /* The actual update must run on the main thread, else Glide won't work */
        mHandler.post(() ->
                SouvenirWidgetProvider.updateAppWidgets(this, manager, widgetIds, firstSouvenir));
    }

}
