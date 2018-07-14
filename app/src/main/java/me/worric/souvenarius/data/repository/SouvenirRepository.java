package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.model.SouvenirResponse;

@Singleton
public class SouvenirRepository {

    private final FirebaseHandler mFirebaseHandler;
    private final StorageHandler mStorageHandler;

    @Inject
    public SouvenirRepository(FirebaseHandler firebaseHandler, StorageHandler storageHandler) {
        mFirebaseHandler = firebaseHandler;
        mStorageHandler = storageHandler;
    }

    public LiveData<List<SouvenirResponse>> getSouvenirs() {
        return Transformations.map(mFirebaseHandler.getResults(), result -> {
            if (Result.Status.SUCCESS.equals(result.status)) {
                return result.response;
            } else if (Result.Status.FAILURE.equals(result.status)) {
                return Collections.emptyList();
            }
            throw new IllegalArgumentException("Unknown status code: " + result.status.name());
        });
    }

    public void addSouvenir(Souvenir souvenir, File image) {
        mStorageHandler.uploadImage(image);
        mFirebaseHandler.storeSouvenir(souvenir);
    }

    public void updateSouvenir(Souvenir souvenir) {
        mFirebaseHandler.storeSouvenir(souvenir);
    }

}
