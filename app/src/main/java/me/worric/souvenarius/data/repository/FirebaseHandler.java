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
    private final DatabaseReference mRef;
    private MutableLiveData<SouvenirRepository.Result<List<SouvenirResponse>>> mResult;

    @Inject
    public FirebaseHandler() {
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(SOUVENIRS_REFERENCE);
    }

    public void fetchSouvenirs() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SouvenirResponse> resultList = parseResponseToList(dataSnapshot);
                SouvenirRepository.Result<List<SouvenirResponse>> result = SouvenirRepository.Result.success(resultList);
                mResult.setValue(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.toException(), "There was a database error");
                mResult.setValue(SouvenirRepository.Result.failure(databaseError.toException()));
            }
        });
    }

    private List<SouvenirResponse> parseResponseToList(DataSnapshot dataSnapshot) {
        List<SouvenirResponse> resultList = new LinkedList<>();
        if (dataSnapshot.hasChildren()) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                SouvenirResponse response = snapshot.getValue(SouvenirResponse.class);
                if (response == null) {
                    Timber.w("Parsed dataSnapshot is null. Continuing...");
                    continue;
                }

                response.setFirebaseId(snapshot.getKey());

                resultList.add(response);
            }
        }
        return resultList;
    }

    public LiveData<SouvenirRepository.Result<List<SouvenirResponse>>> getResults() {
        if (mResult == null) {
            mResult = new MutableLiveData<>();
            fetchSouvenirs();
        }
        return mResult;
    }

    public void storeSouvenir(Souvenir souvenir) {
        String pushKey = mRef.push().getKey();
        if (pushKey == null) throw new IllegalStateException("Key cannot be null!");

        mRef.child(pushKey).setValue(souvenir);
    }

}
