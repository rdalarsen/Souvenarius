package me.worric.souvenarius.ui.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import timber.log.Timber;

public final class NetUtils {

    public static final String STORAGE_PATH = "images";

    private NetUtils() {
    }

    public static boolean getIsConnected(@NonNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) throw new IllegalStateException("ConnectivityManager is null - should not happen!");

        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static StorageReference getStorageReferenceForCurrentUser(String imageName) {
        String uid = FirebaseAuth.getInstance().getUid();
        Timber.i("getStorageReferenceForCurrentUser called; imageName=%s,uid=%s", imageName, uid);
        if (TextUtils.isEmpty(uid)) {
            return null;
        }
        if (TextUtils.isEmpty(imageName)) {
            return null;
        }
        return FirebaseStorage.getInstance().getReference().child(uid).child(imageName);
    }

    public static StorageReference getStorageReferenceForAllUsers(String imageName) {
        Timber.i("getStorageReferenceForAllUsers called; imageName=%s", imageName);
        if (TextUtils.isEmpty(imageName)) {
            return null;
        }
        return FirebaseStorage.getInstance().getReference(STORAGE_PATH).child(imageName);
    }

}
