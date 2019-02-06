package me.worric.souvenarius.data.repository.souvenir;

import android.net.Uri;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;

public interface StorageHandler {

    void uploadPhoto(@NonNull File imageFile);

    void uploadPhoto(@NonNull File imageFile, @NonNull Uri fileUri);

    void removePhoto(@NonNull String photoName);

    void removePhotos(@NonNull List<String> photos);

}
