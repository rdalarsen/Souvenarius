package me.worric.souvenarius.ui.common;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public final class FileUtils {

    public static final String FILE_PROVIDER_AUTHORITY = "me.worric.souvenarius.fileprovider";
    private static final String DATE_PATTERN = "yyyyMMdd_HHmmss";
    private static final String FILE_NAME_PREFIX = "JPEG_";
    private static final String FILE_NAME_SEPARATOR = "_";
    private static final String FILE_NAME_SUFFIX = ".jpg";

    private FileUtils() {}

    public static File getLocalFileForPhotoName(@NonNull String photoName, @NonNull Context context) {
        if (TextUtils.isEmpty(photoName)) {
            return null;
        }
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, photoName);
    }

    public static File createTempImageFile(@NonNull Context context) {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        String imageFileName = FILE_NAME_PREFIX + timestamp + FILE_NAME_SEPARATOR;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, FILE_NAME_SUFFIX, storageDir);
        } catch (IOException e) {
            Timber.e(e, "Unable to create temp file; returning null...");
            return null;
        }
    }

    public static Uri getUriForFile(@NonNull File file, @NonNull Context context) {
        return FileProvider.getUriForFile(context,
                FILE_PROVIDER_AUTHORITY,
                file);
    }

}
