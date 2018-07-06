package me.worric.souvenarius.ui.common;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import me.worric.souvenarius.ui.GlideApp;

public class BindingAdapters {

    @BindingAdapter({"imageUri", "placeholder"})
    public static void loadImage(ImageView view, String uri, Drawable placeholder) {
        GlideApp.with(view.getContext())
                .load(uri)
                .placeholder(placeholder)
                .into(view);
    }

}
