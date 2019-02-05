package me.worric.souvenarius.ui.common;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.ui.GlideLoaderProvider;
import me.worric.souvenarius.ui.GlideRequest;
import me.worric.souvenarius.ui.search.SearchResultsAdapter;
import me.worric.souvenarius.ui.signin.SignInViewModel;
import timber.log.Timber;

public class BindingAdapters {

    @BindingAdapter("errorText")
    public static void setError(TextInputLayout layout, SignInViewModel.SignInError oldError,
                                SignInViewModel.SignInError error) {
        if (error != null ) {
            layout.setError(error.getErrorText());
        }
    }

    @BindingAdapter("errorEnabled")
    public static void setErrorEnabled(TextInputLayout layout, boolean oldValue, boolean value) {
        if (oldValue != value) {
            layout.setErrorEnabled(value);
        }
    }

    @BindingAdapter({"searchAdapter", "searchResults"})
    public static void updateSearchResults(RecyclerView recyclerView, SearchResultsAdapter adapter,
                                           Result<List<SouvenirDb>> result) {
        adapter.swapItems(result);
    }

    @BindingAdapter("itemDecoration")
    public static void setItemDecoration(RecyclerView view, RecyclerView.ItemDecoration oldValue,
                                         RecyclerView.ItemDecoration value) {
        if (oldValue != null) {
            view.removeItemDecoration(oldValue);
        }
        if (value != null) {
            view.addItemDecoration(value);
        }
    }

    @BindingAdapter("goneUnless")
    public static void goneUnless(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

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

        GlideLoaderProvider.getInstance().loadImageFromLocalFile(view.getContext(), photoFile)
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
            if (TextUtils.isEmpty(editText.getText())) {
                String locationString = editText.getContext().getString(R.string.format_location_add,
                        locationResult.response.getLocality(),
                        locationResult.response.getCountryName());
                editText.setText(locationString);
            }
        } else {
            editText.setHint(R.string.hint_location_error_add);
        }
    }

    @BindingAdapter({"imageName"})
    public static void loadImageFromName(ImageView view, String imageName) {
        File localPhoto = FileUtils.getLocalFileForPhotoName(imageName, view.getContext());
        GlideRequest<Drawable> request;

        if (localPhoto != null && localPhoto.exists()) {
            request = GlideLoaderProvider.getInstance()
                    .loadImageFromLocalFile(view.getContext(), localPhoto);
        } else {
            StorageReference reference = NetUtils.getStorageReferenceForAllUsers(imageName);
            request = GlideLoaderProvider.getInstance()
                    .loadImageFromFirebaseStorage(view.getContext(), reference);
        }

        request.into(view);
    }

}
