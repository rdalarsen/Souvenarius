package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        return mFirebaseHandler.getSouvenirs();
    }

    public void addSouvenir(Souvenir souvenir, File image) {
        mStorageHandler.uploadImage(image);
        mFirebaseHandler.storeSouvenir(souvenir);
    }

}
