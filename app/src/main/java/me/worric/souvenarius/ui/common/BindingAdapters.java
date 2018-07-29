package me.worric.souvenarius.ui.common;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.RequestBuilder;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import me.worric.souvenarius.ui.GlideApp;
import timber.log.Timber;

public class BindingAdapters {

    @BindingAdapter({"imageName"})
    public static void loadImageFromName(ImageView view, String imageName) {
        Timber.d("imageName is: %s", imageName);
        File localPhoto = FileUtils.getLocalFileForPhotoName(imageName, view.getContext());
        RequestBuilder<Drawable> requestBuilder;

        if (localPhoto != null && localPhoto.exists()) {
            Timber.d("local photo existed, loading it...");
            requestBuilder = GlideApp.with(view.getContext()).load(localPhoto);
        } else {
            Timber.d("local photo DID NOT exist, trying to load FirebaseStorage photo...");
            StorageReference reference = NetUtils.getStorageReferenceForAllUsers(imageName);
            requestBuilder = GlideApp.with(view.getContext()).load(reference);
        }

        requestBuilder.into(view);
    }

}
