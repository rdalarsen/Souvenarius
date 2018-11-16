package me.worric.souvenarius.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class MockGlideLoaderImpl implements GlideLoader {

    private static final String ASSET_FILE_NAME = "default_image.jpg";

    @Override
    public GlideRequest<Drawable> loadImageFromLocalFile(Context context, File localPhoto) {
        Timber.d("Mock: Loading real file from local storage as usual");
        return GlideApp
                .with(context)
                .load(localPhoto);
    }

    @Override
    public GlideRequest<Drawable> loadImageFromFirebaseStorage(Context context, StorageReference reference) {
        Timber.d("Mock: Simulate loading image from Storage, while actually loading from from assets");
        AssetManager am = context.getResources().getAssets();
        Bitmap defaultBitmapFromAssets = loadBitmapFromAssets(am);
        return GlideApp
                .with(context)
                .load(defaultBitmapFromAssets);
    }

    private Bitmap loadBitmapFromAssets(AssetManager am) {
        try (InputStream assetInputStream = am.open(ASSET_FILE_NAME)) {
            return BitmapFactory.decodeStream(assetInputStream);
        } catch (IOException e) {
            Timber.e(e, "Mock: Could not open asset file! Name=%s", ASSET_FILE_NAME);
            return null;
        }
    }

}
