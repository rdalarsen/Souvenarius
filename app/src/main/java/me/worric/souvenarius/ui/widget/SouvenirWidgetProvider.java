package me.worric.souvenarius.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.net.Uri;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.ui.GlideApp;
import me.worric.souvenarius.ui.common.FileUtils;
import timber.log.Timber;

/**
 * For loading images through Glide in AppWidgets,
 * see: <a href="https://futurestud.io/tutorials/glide-loading-images-into-notifications-and-appwidgets">this guide</a>.
 */
public class SouvenirWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, SouvenirDb souvenirDb) {
        Timber.i("updateAppWidget called");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.souvenir_widget);
        views.setTextViewText(R.id.widget_hello_world, "This is the first photo");

        String photoFileName = "JPEG_20180720_160610_8510624041652845276.jpg";
        File localPhotoFile = FileUtils.getLocalFileForPhotoName(photoFileName, context);

        AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.iv_widget_souvenir_photo, views, appWidgetId);
        if (localPhotoFile != null && localPhotoFile.exists()) {
            // Load local file via Glide
            Uri uriForLocalFile = FileUtils.getUriForFile(localPhotoFile, context);
            GlideApp.with(context.getApplicationContext())
                    .asBitmap()
                    .load(uriForLocalFile)
                    .centerCrop()
                    .into(appWidgetTarget);
        } else {
            // Load image hosted on Firebase image via Glide
            GlideApp.with(context.getApplicationContext())
                    .asBitmap()
                    .load(FirebaseStorage.getInstance().getReference("images").child(photoFileName))
                    .centerCrop()
                    .into(appWidgetTarget);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void updateAppWidgets(Context context, AppWidgetManager manager, int[] widgetIds,
                                 SouvenirDb souvenirDb) {
        Timber.i("updateAppWidgets called");
        for (int appWidgetId : widgetIds) {
            updateAppWidget(context, manager, appWidgetId, souvenirDb);
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

