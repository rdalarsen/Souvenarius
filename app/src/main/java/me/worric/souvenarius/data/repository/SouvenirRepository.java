package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import me.worric.souvenarius.data.model.Souvenir;

@Singleton
public class SouvenirRepository {

    private final MutableLiveData<String> mHelloWorld;
    private final FirebaseHandler mFirebaseHandler;

    @Inject
    public SouvenirRepository(@Named("theMessage") String theMessage, FirebaseHandler firebaseHandler) {
        mHelloWorld = new MutableLiveData<>();
        mFirebaseHandler = firebaseHandler;
        mHelloWorld.setValue(theMessage);
    }

    public LiveData<String> getHelloWorldText() {
        return mHelloWorld;
    }

    public LiveData<List<Souvenir>> getSouvenirs() {
        return mFirebaseHandler.getSouvenirs();
    }

    public void addSouvenir(Souvenir souvenir) {
        mFirebaseHandler.addSouvenir(souvenir);
    }

}
