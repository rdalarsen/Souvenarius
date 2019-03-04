package me.worric.souvenarius.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.firebase.storage.StorageReference;

import java.io.File;

public interface GlideLoader {

    GlideRequest<Drawable> loadImageFromLocalFile(Context context, File localPhoto);

    GlideRequest<Drawable> loadImageFromFirebaseStorage(Context context, StorageReference reference);

}
