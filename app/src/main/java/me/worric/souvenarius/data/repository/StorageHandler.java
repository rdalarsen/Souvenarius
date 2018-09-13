package me.worric.souvenarius.data.repository;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

public interface StorageHandler {

    void uploadPhoto(@NonNull File imageFile);

    void removePhoto(@NonNull String photoName);

    void removePhotos(@NonNull List<String> photos);

}
