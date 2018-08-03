package me.worric.souvenarius.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.ui.GlideApp;
import me.worric.souvenarius.ui.common.FileUtils;
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.main.MainActivity;
import timber.log.Timber;

/**
 * For loading images through Glide in AppWidgets,
 * see: <a href="https://futurestud.io/tutorials/glide-loading-images-into-notifications-and-appwidgets">this guide</a>.
 */
public class SouvenirWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_LAUNCH_ADD_SOUVENIR = "action_widget_launch_add_souvenir";
    public static final String ACTION_WIDGET_LAUNCH_SOUVENIR_DETAILS = "action_widget_launch_souvenir_details";
    public static final String EXTRA_SOUVENIR_ID = "extra_souvenir_id";
    public static final int RC_LAUNCH_ADD_SOUVENIR = 0;
    public static final int RC_LAUNCH_SOUVENIR_DETAILS = 1;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Result<SouvenirDb> souvenirDb) {
        Timber.i("updateAppWidget called");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.souvenir_widget);

        Timber.w("The message of the result is: %s", souvenirDb.message);

        if (souvenirDb.status.equals(Result.Status.SUCCESS)) {

            String photoFileName = null;
            File localPhotoFile = null;
            if (!TextUtils.isEmpty(souvenirDb.response.getFirstPhoto())) {
                photoFileName = souvenirDb.response.getFirstPhoto();
                localPhotoFile = FileUtils.getLocalFileForPhotoName(photoFileName, context);
            }

            AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.iv_widget_souvenir_photo, views, appWidgetId);
            if (localPhotoFile != null && localPhotoFile.exists()) {
                // Load local file via Glide
                Uri uriForLocalFile = FileUtils.getUriForFile(localPhotoFile, context);
                GlideApp.with(context.getApplicationContext())
                        .asBitmap()
                        .load(uriForLocalFile)
                        .placeholder(R.drawable.ic_photo)
                        .error(R.drawable.ic_broken_image)
                        .centerCrop()
                        .into(appWidgetTarget);
            } else if (!TextUtils.isEmpty(photoFileName)){
                // Load image hosted on Firebase image via Glide
                GlideApp.with(context.getApplicationContext())
                        .asBitmap()
                        .load(FirebaseStorage.getInstance().getReference(NetUtils.STORAGE_PATH)
                                .child(photoFileName))
                        .placeholder(R.drawable.ic_photo)
                        .error(R.drawable.ic_broken_image)
                        .centerCrop()
                        .into(appWidgetTarget);
            }

            views.setOnClickPendingIntent(R.id.iv_widget_souvenir_photo,
                    createSouvenirDetailsPendingIntent(context, souvenirDb));
        } else {
            views.setViewVisibility(R.id.iv_widget_souvenir_photo, View.GONE);
            views.setViewVisibility(R.id.tv_widget_error_text, View.VISIBLE);
            views.setTextViewText(R.id.tv_widget_error_text, souvenirDb.message);
        }

        views.setOnClickPendingIntent(R.id.btn_widget_add_souvenir,
                createAddSouvenirPendingIntent(context));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static PendingIntent createAddSouvenirPendingIntent(Context context) {
        Intent addSouvenirIntent = new Intent(context, MainActivity.class);
        addSouvenirIntent.setAction(ACTION_WIDGET_LAUNCH_ADD_SOUVENIR);
        addSouvenirIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, RC_LAUNCH_ADD_SOUVENIR, addSouvenirIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent createSouvenirDetailsPendingIntent(Context context, Result<SouvenirDb> souvenirDb) {
        Intent souvenirDetailsIntent = new Intent(context, MainActivity.class);
        souvenirDetailsIntent.setAction(ACTION_WIDGET_LAUNCH_SOUVENIR_DETAILS);
        souvenirDetailsIntent.putExtra(EXTRA_SOUVENIR_ID, souvenirDb.response.getId());
        souvenirDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, RC_LAUNCH_SOUVENIR_DETAILS,
                souvenirDetailsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    static void updateAppWidgets(Context context, AppWidgetManager manager, int[] widgetIds,
                                 Result<SouvenirDb> souvenirResult) {
        Timber.i("updateAppWidgets called");
        for (int appWidgetId : widgetIds) {
            updateAppWidget(context, manager, appWidgetId, souvenirResult);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.i("onUpdate called");
        UpdateWidgetService.startWidgetUpdate(context);
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }

}

