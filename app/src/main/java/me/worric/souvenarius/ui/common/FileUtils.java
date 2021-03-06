package me.worric.souvenarius.ui.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import timber.log.Timber;

public final class FileUtils {

    public static final String FILE_PROVIDER_AUTHORITY = "me.worric.souvenarius.fileprovider";
    public static final int PHOTO_WIDTH = 500;
    public static final int PHOTO_HEIGHT = 500;
    private static final String DATE_PATTERN = "yyyyMMdd_HHmmss";
    private static final String FILE_NAME_PREFIX = "JPEG_";
    private static final String FILE_NAME_SEPARATOR = "_";
    private static final String FILE_NAME_SUFFIX = ".jpg";
    private static final int JPEG_QUALITY = 50;

    private FileUtils() {
    }

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

    /**
     * The code in this method is based on
     * <a href="https://developer.android.com/topic/performance/graphics/load-bitmap">Google's guide</a>.
     */
    public static void persistOptimizedBitmapToDisk(File photoFile, int width, int height) {
        if (photoFile == null) {
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

        options.inSampleSize = getOptimalSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        Bitmap optimizedBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

        writeBitmapFileToDisk(photoFile, optimizedBitmap);
    }

    /**
     * The code in this method is heavily based on
     * <a href="https://developer.android.com/topic/performance/graphics/load-bitmap">Google's guide</a>,
     * but customized for own requirements and code style.
     */
    private static int getOptimalSampleSize(BitmapFactory.Options options, int width, int height) {
        int outHeight = options.outHeight;
        int outWidth = options.outWidth;

        int optimalSampleSize = 1;
        if (outHeight > height || outWidth > width) {
            int thirdOfHeight = outHeight / 3;
            int thirdOfWidth = outWidth / 3;

            while ((thirdOfHeight / optimalSampleSize) >= height && (thirdOfWidth / optimalSampleSize) >= width) {
                optimalSampleSize *= 2;
            }
        }

        return optimalSampleSize;
    }

    private static void writeBitmapFileToDisk(File photoFile, Bitmap bitmap) {
        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
        } catch (FileNotFoundException e) {
            Timber.e(e,"File not found");
        } catch (IOException e) {
            Timber.e(e, "There was an IO error");
        }
    }

}
