package me.worric.souvenarius.ui.common;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.RequestBuilder;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.ui.GlideApp;
import timber.log.Timber;

public class BindingAdapters {

    @BindingAdapter("dialogText")
    public static void dialogText(EditText editText, String dialogText) {
        if (TextUtils.isEmpty(editText.getText().toString())) {
            editText.setText(dialogText);
        }
    }

    @BindingAdapter("photoFile")
    public static void photoFile(ImageView view, File photoFile) {
        if (photoFile == null) {
            Timber.w("photoFile was null; returning");
            return;
        }

        GlideApp.with(view.getContext())
                .load(photoFile)
                .error(android.R.color.transparent)
                .centerCrop()
                .into(view);
    }

    @BindingAdapter("locationResult")
    public static void inputLocation(EditText editText, Result<Address> locationResult) {
        if (locationResult == null) {
            Timber.w("location was null; returning");
            return;
        }

        if (locationResult.status.equals(Result.Status.SUCCESS)) {
            Timber.i("result of location is successful. Location is: %s,%s",
                    locationResult.response.getLocality(),
                    locationResult.response.getCountryName());
            if (TextUtils.isEmpty(editText.getText())) {
                String locationString = editText.getContext().getString(R.string.format_location_add,
                        locationResult.response.getLocality(),
                        locationResult.response.getCountryName());
                editText.setText(locationString);
            }
        } else {
            Timber.w("Result unsuccessful. Message=%s", locationResult.message);
            editText.setHint(R.string.hint_location_error_add);
        }
    }

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
