package me.worric.souvenarius.ui.common;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import me.worric.souvenarius.ui.GlideApp;
import timber.log.Timber;

public class BindingAdapters {

    @BindingAdapter({"imageUri", "placeholder"})
    public static void loadImage(ImageView view, String uri, Drawable placeholder) {
        GlideApp.with(view.getContext())
                .load(uri)
                .placeholder(placeholder)
                .into(view);
    }

    @BindingAdapter({"imageName"})
    public static void loadImageFromName(ImageView view, String imageName) {
        Timber.d("imageName is: %s", imageName);
        StorageReference reference = FirebaseStorage.getInstance()
                .getReference("images")
                .child(imageName);
        GlideApp.with(view.getContext())
                .load(reference)
                .into(view);
    }

}
