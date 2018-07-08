package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.model.SouvenirResponse;
import timber.log.Timber;

public class FirebaseHandler {

    private static final String SOUVENIRS_REFERENCE = "souvenirs";
    private final FirebaseDatabase mDatabase;
    private final MutableLiveData<List<SouvenirResponse>> mSouvenirs;
    private final DatabaseReference mRef;

    @Inject
    public FirebaseHandler() {
        mSouvenirs = new MutableLiveData<>();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(SOUVENIRS_REFERENCE);
        fetchSouvenirs();
    }

    public void fetchSouvenirs() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SouvenirResponse> resultList = new LinkedList<>();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        SouvenirResponse response = snapshot.getValue(SouvenirResponse.class);
                        if (response == null) continue;

                        response.setFirebaseId(snapshot.getKey());

                        resultList.add(response);
                    }
                }

                mSouvenirs.setValue(resultList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.toException(), "There was a database error");
            }
        });
    }

    public LiveData<List<SouvenirResponse>> getSouvenirs() {
        return mSouvenirs;
    }

    public void storeSouvenir(Souvenir souvenir) {
        String pushKey = mRef.push().getKey();
        if (pushKey == null) throw new IllegalStateException("Key cannot be null!");

        mRef.child(pushKey).setValue(souvenir);
    }

}
