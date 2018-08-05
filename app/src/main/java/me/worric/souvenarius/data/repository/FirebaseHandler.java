package me.worric.souvenarius.data.repository;

import android.support.annotation.NonNull;
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
public class FirebaseHandler {

    private static final String SOUVENIRS_REFERENCE = "souvenirs";
    private final FirebaseAuth mAuth;
    private final DatabaseReference mRef;
    private final Map<Integer,String> mErrorMessages;
    private ValueEventListener mValueEventListener = null;

    @Inject
    public FirebaseHandler(@FirebaseErrorMsgs Map<Integer,String> errorMessages) {
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference(SOUVENIRS_REFERENCE);
        mErrorMessages = errorMessages;
    }

    public void fetchSouvenirsForCurrentUser(@NonNull OnResultListener listener) {
        if (TextUtils.isEmpty(mAuth.getUid())) {
            Timber.w("Current user is null; not retrieving souvenirs...");
            listener.onResult(Result.failure(mErrorMessages
                    .get(R.string.error_message_firebase_not_signed_in)));
            return;
        } else if (mValueEventListener != null) {
            Timber.w("Already executing request; returning...");
            listener.onResult(Result.failure(mErrorMessages
                    .get(R.string.error_message_firebase_already_executing)));
            return;
        }

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SouvenirDb> resultList = parseResponseToList(dataSnapshot);
                listener.onResult(Result.success(resultList));
                mValueEventListener = null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.toException(), "There was a database error");
                listener.onResult(Result.failure(databaseError.getMessage()));
                mValueEventListener = null;
            }
        };

        mRef.child(mAuth.getUid()).addListenerForSingleValueEvent(mValueEventListener);
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

    public void storeSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener) {
        mRef.child(souvenir.getUid()).child(souvenir.getId()).setValue(souvenir, completionListener);
    }

    public void deleteSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener) {
        mRef.child(souvenir.getUid()).child(souvenir.getId()).removeValue(completionListener);
    }

    public interface OnResultListener {
        void onResult(Result<List<SouvenirDb>> souvenirs);
    }

}
