package me.worric.souvenarius.data.repository;

import android.net.Uri;
import android.support.annotation.NonNull;

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

    public void uploadPhoto(@NonNull File imageFile) {
        mRef.child(imageFile.getName()).putFile(Uri.fromFile(imageFile)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) Timber.e(task.getException(),"Could not upload the photo.");
        });
    }

    public void removePhoto(@NonNull String photoName) {
        mRef.child(photoName).delete().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) Timber.e(task.getException(), "Could not delete the photo.");
        });
    }

    public void removePhotos(@NonNull List<String> photos) {
        for (String photoName : photos) {
            mRef.child(photoName).delete().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) Timber.e(task.getException(), "Could not delete all photos.");
            });
        }
    }

}
