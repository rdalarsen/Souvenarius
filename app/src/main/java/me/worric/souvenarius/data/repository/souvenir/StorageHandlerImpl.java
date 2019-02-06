package me.worric.souvenarius.data.repository.souvenir;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import timber.log.Timber;

import static me.worric.souvenarius.ui.common.NetUtils.STORAGE_PATH;

@Singleton
public class StorageHandlerImpl implements StorageHandler {

    private final StorageReference mRef;

    @Inject
    public StorageHandlerImpl() {
        this(FirebaseStorage.getInstance().getReference(STORAGE_PATH));
    }

    public StorageHandlerImpl(StorageReference ref) {
        mRef = ref;
    }

    @Override
    public void uploadPhoto(@NonNull File imageFile) {
        uploadPhoto(imageFile, Uri.fromFile(imageFile));
    }

    @Override
    public void uploadPhoto(@NonNull File imageFile, @NonNull Uri fileUri) {
        mRef.child(imageFile.getName()).putFile(fileUri).addOnCompleteListener(task -> {
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
