package me.worric.souvenarius.data.repository;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class StorageHandler {

    private static final String STORAGE_REFERENCE = "images";
    private final FirebaseAuth mAuth;
    private final StorageReference mRef;

    @Inject
    public StorageHandler() {
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseStorage.getInstance().getReference(STORAGE_REFERENCE);
    }

    public void uploadImage(@NonNull File imageFile) {
        if (TextUtils.isEmpty(mAuth.getUid())) {
            Timber.i("User null, not uploading photo");
            return;
        }

        mRef.child(imageFile.getName()).putFile(Uri.fromFile(imageFile)).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri uploadUri = task.getResult().getUploadSessionUri();
                Timber.i("Upload URI: %s", uploadUri.toString());
            } else {
                Timber.e("DID NOT upload the file! isCanceled: %s. isComplete: %s. isSuccessful: %s",
                        task.isCanceled(),
                        task.isComplete(),
                        task.isSuccessful());
            }
        });
    }

    public void removeImage(@NonNull String photoName) {
        mRef.child(photoName).delete().addOnCompleteListener(task -> {
            Timber.i("The DELETE task is successful? %s", task.isSuccessful());
            if (!task.isSuccessful()) Timber.e(task.getException(), "Task DELETE didn't execute right!");
        });
    }

    public void removeImages(@NonNull List<String> photos) {
        if (photos.isEmpty()) {
            Timber.w("list of photos for the current souvenir is empty. Skipping deletion...");
            return;
        }

        for (String photoName : photos) {
            Timber.i("attempting deletion of all photos of the souvenir");
            mRef.child(photoName).delete().addOnCompleteListener(task -> {
                Timber.i("The DELETE task is successful? %s", task.isSuccessful());
                if (!task.isSuccessful()) Timber.e(task.getException(), "Task DELETE didn't execute right!");
            });
        }
    }

}
