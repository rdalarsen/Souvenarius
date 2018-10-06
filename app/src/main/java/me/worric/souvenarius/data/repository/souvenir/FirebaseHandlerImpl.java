package me.worric.souvenarius.data.repository.souvenir;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.di.FirebaseErrorMsgs;
import timber.log.Timber;

@Singleton
public class FirebaseHandlerImpl implements FirebaseHandler {

    private static final String SOUVENIRS_REFERENCE = "souvenirs";
    private final FirebaseAuth mAuth;
    private final DatabaseReference mRef;
    private final Map<Integer,String> mErrorMessages;
    private CustomEventListener mValueEventListener = null;

    @Inject
    public FirebaseHandlerImpl(@FirebaseErrorMsgs Map<Integer,String> errorMessages) {
        this(FirebaseAuth.getInstance(), FirebaseDatabase.getInstance().getReference(SOUVENIRS_REFERENCE),
                errorMessages);
    }

    public FirebaseHandlerImpl(FirebaseAuth auth, DatabaseReference ref, Map<Integer, String> errorMessages) {
        mAuth = auth;
        mRef = ref;
        mErrorMessages = errorMessages;
    }

    @Override
    public void fetchSouvenirsForCurrentUser(@NonNull OnResultListener listener) {
        fetchSouvenirsForCurrentUser(listener, getDefaultValueEventListener(listener));
    }

    private CustomEventListener getDefaultValueEventListener(@NonNull OnResultListener listener) {
        return new CustomEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                super.onDataChange(dataSnapshot);
                List<SouvenirDb> resultList = parseResponseToList(dataSnapshot);
                listener.onResult(Result.success(resultList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                super.onCancelled(databaseError);
                Timber.e(databaseError.toException(), "There was a database error");
                listener.onResult(Result.failure(databaseError.getMessage()));
            }
        };
    }

    private List<SouvenirDb> parseResponseToList(DataSnapshot dataSnapshot) {
        List<SouvenirDb> resultList = new LinkedList<>();
        if (dataSnapshot.hasChildren()) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                SouvenirDb response = snapshot.getValue(SouvenirDb.class);
                if (response == null) {
                    Timber.w("Parsed dataSnapshot is null. Continuing...");
                    continue;
                }

                response.setId(snapshot.getKey());

                resultList.add(response);
            }
        }
        return resultList;
    }

    @Override
    public void fetchSouvenirsForCurrentUser(@NonNull OnResultListener listener,
                                             @NonNull CustomEventListener valueEventListener) {
        if (TextUtils.isEmpty(mAuth.getUid())) {
            Timber.w("Current user is null; not retrieving souvenirs...");
            listener.onResult(Result.failure(mErrorMessages
                    .get(R.string.error_message_firebase_not_signed_in)));
            return;
        }

        if (mValueEventListener != null && mValueEventListener.isRunning()) {
            Timber.w("Already executing request; returning...");
            listener.onResult(Result.failure(mErrorMessages
                    .get(R.string.error_message_firebase_already_executing)));
            return;
        }

        initEventListener(valueEventListener);

        mRef.child(mAuth.getUid()).addListenerForSingleValueEvent(mValueEventListener);
    }

    private void initEventListener(CustomEventListener valueEventListener) {
        mValueEventListener = valueEventListener;
        mValueEventListener.setRunning(true);
    }

    @Override
    public void storeSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener) {
        mRef.child(souvenir.getUid()).child(souvenir.getId()).setValue(souvenir, completionListener);
    }

    @Override
    public void deleteSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener) {
        mRef.child(souvenir.getUid()).child(souvenir.getId()).removeValue(completionListener);
    }

    @VisibleForTesting
    public void setValueEventListener(@NonNull CustomEventListener valueEventListener) {
        mValueEventListener = valueEventListener;
    }

    public static class CustomEventListener implements ValueEventListener {

        private boolean isRunning = false;

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            setRunning(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            setRunning(false);
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }

}
