package me.worric.souvenarius.data.repository.souvenir;

import android.net.Uri;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import timber.log.Timber;

@Singleton
public class MockStorageHandlerImpl implements StorageHandler {

    @Inject
    public MockStorageHandlerImpl() {
    }

    @Override
    public void uploadPhoto(@NonNull File imageFile) {
        Timber.d("Mock response: Successfully uploaded photo. Name=%s", imageFile.getName());
    }

    @Override
    public void uploadPhoto(@NonNull File imageFile, @NonNull Uri fileUri) {
        Timber.d("Mock response: Successfully uploaded photo. Name=%s,uri=%s",
                imageFile.getName(),
                fileUri.toString());
    }

    @Override
    public void removePhoto(@NonNull String photoName) {
        Timber.d("Mock response: Successfully removed photo. Name=%s", photoName);
    }

    @Override
    public void removePhotos(@NonNull List<String> photos) {
        Timber.d("Mock response: Successfully removed photos. Names=%s",
                getStringFromPhotoList(photos));
    }

    private String getStringFromPhotoList(List<String> photos) {
        ListIterator<String> it = photos.listIterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append(it.hasNext() ? ", " : ".");
        }
        return sb.toString();
    }

}
