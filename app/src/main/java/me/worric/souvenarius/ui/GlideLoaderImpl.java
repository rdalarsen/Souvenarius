package me.worric.souvenarius.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.firebase.storage.StorageReference;

import java.io.File;

public class GlideLoaderImpl implements GlideLoader {

    @Override
    public GlideRequest<Drawable> loadImageFromLocalFile(Context context, File localPhoto) {
        return GlideApp
                .with(context)
                .load(localPhoto);
    }

    @Override
    public GlideRequest<Drawable> loadImageFromFirebaseStorage(Context context, StorageReference reference) {
        return GlideApp
                .with(context)
                .load(reference);
    }

}
