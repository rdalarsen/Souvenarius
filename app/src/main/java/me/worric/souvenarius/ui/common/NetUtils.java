package me.worric.souvenarius.ui.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;

public final class NetUtils {

    public static final String STORAGE_PATH = "images";

    private NetUtils() {
    }

    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) throw new IllegalStateException("ConnectivityManager is null - should not happen!");

        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static StorageReference getStorageReferenceForAllUsers(String imageName) {
        if (TextUtils.isEmpty(imageName)) {
            return null;
        }
        return FirebaseStorage.getInstance().getReference(STORAGE_PATH).child(imageName);
    }

}
