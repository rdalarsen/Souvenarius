package me.worric.souvenarius.data.repository.souvenir;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;

@Singleton
public class MockFirebaseHandlerImpl implements FirebaseHandler {

    @Inject
    public MockFirebaseHandlerImpl() {
    }

    @Override
    public void fetchSouvenirsForCurrentUser(@NonNull OnResultListener listener) {
        fetchSouvenirsForCurrentUser(listener, new FirebaseHandlerImpl.CustomEventListener());
    }

    @Override
    public void fetchSouvenirsForCurrentUser(@NonNull OnResultListener listener, @NonNull FirebaseHandlerImpl.CustomEventListener valueEventListener) {
        listener.onResult(Result.failure("There was an error in the request"));
    }

    @Override
    public void storeSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener) {
        completionListener.onComplete(null, null);
    }

    @Override
    public void deleteSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener) {
        completionListener.onComplete(null, null);
    }

}
