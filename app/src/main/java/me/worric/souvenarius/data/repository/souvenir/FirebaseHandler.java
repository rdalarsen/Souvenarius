package me.worric.souvenarius.data.repository.souvenir;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

import androidx.annotation.NonNull;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;

public interface FirebaseHandler {

    void fetchSouvenirsForCurrentUser(@NonNull OnResultListener listener);

    void fetchSouvenirsForCurrentUser(@NonNull OnResultListener listener, @NonNull FirebaseHandlerImpl.CustomEventListener valueEventListener);

    void storeSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener);

    void deleteSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener);

    interface OnResultListener {
        void onResult(Result<List<SouvenirDb>> souvenirs);
    }

}
