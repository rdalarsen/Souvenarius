package me.worric.souvenarius.data.repository;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

import static me.worric.souvenarius.ui.common.NetUtils.STORAGE_PATH;

@Singleton
public class StorageHandlerImpl implements StorageHandler {

    private final StorageReference mRef;

    @Inject
    public StorageHandlerImpl() {
        mRef = FirebaseStorage.getInstance().getReference(STORAGE_PATH);
    }

    @Override
    public void uploadPhoto(@NonNull File imageFile) {
        mRef.child(imageFile.getName()).putFile(Uri.fromFile(imageFile)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) Timber.e(task.getException(),"Could not upload the photo.");
        });
    }

    @Override
    public void removePhoto(@NonNull String photoName) {
        mRef.child(photoName).delete().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) Timber.e(task.getException(), "Could not delete the photo.");
        });
    }

    @Override
    public void removePhotos(@NonNull List<String> photos) {
        for (String photoName : photos) {
            mRef.child(photoName).delete().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) Timber.e(task.getException(), "Could not delete all photos.");
            });
        }
    }

}
