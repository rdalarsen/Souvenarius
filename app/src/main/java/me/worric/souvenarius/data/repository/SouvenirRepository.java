package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.data.model.Souvenir;

@Singleton
public class SouvenirRepository {

    private final FirebaseHandler mFirebaseHandler;
    private final StorageHandler mStorageHandler;

    @Inject
    public SouvenirRepository(FirebaseHandler firebaseHandler, StorageHandler storageHandler) {
        mFirebaseHandler = firebaseHandler;
        mStorageHandler = storageHandler;
    }

    public LiveData<List<Souvenir>> getSouvenirs() {
        return mFirebaseHandler.getSouvenirs();
    }

    public void addSouvenir(Souvenir souvenir) {
        mFirebaseHandler.addSouvenir(souvenir);
    }

    public void save(File imageFile) {
        mStorageHandler.uploadImage(imageFile);
    }

}
