package me.worric.souvenarius.data.repository;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import javax.inject.Inject;

import timber.log.Timber;

public class StorageHandler {

    private static final String STORAGE_REFERENCE = "images";
    private final FirebaseStorage mFirebaseStorage;
    private final StorageReference mRef;

    @Inject
    public StorageHandler() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mRef = mFirebaseStorage.getReference(STORAGE_REFERENCE);
    }

    public void uploadImage(@NonNull File imageFile) {
        StorageReference ref = mRef.child(imageFile.getName());
        ref.putFile(Uri.fromFile(imageFile)).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri uploadUri = task.getResult().getUploadSessionUri();
                Timber.i("Upload URI: %s", uploadUri.toString());
            }
        });
    }

}
