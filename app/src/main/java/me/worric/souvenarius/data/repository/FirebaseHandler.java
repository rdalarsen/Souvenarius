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

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import timber.log.Timber;

public class FirebaseHandler {

    private static final String SOUVENIRS_REFERENCE = "souvenirs";
    private final FirebaseDatabase mDatabase;
    private final DatabaseReference mRef;
    private MutableLiveData<Result<List<SouvenirDb>>> mResult;

    @Inject
    public FirebaseHandler() {
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(SOUVENIRS_REFERENCE);
    }

    public void fetchSouvenirs() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SouvenirDb> resultList = parseResponseToList(dataSnapshot);
                Result<List<SouvenirDb>> result = Result.success(resultList);
                mResult.setValue(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.toException(), "There was a database error");
                mResult.setValue(Result.failure(databaseError.toException().getMessage()));
            }
        });
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

                response.setId(Long.parseLong(snapshot.getKey()));

                resultList.add(response);
            }
        }
        return resultList;
    }

    public LiveData<Result<List<SouvenirDb>>> getResults() {
        if (mResult == null) {
            mResult = new MutableLiveData<>();
            fetchSouvenirs();
        }
        return mResult;
    }

    public void storeSouvenir(SouvenirDb souvenir, DatabaseReference.CompletionListener completionListener) {
        /*String pushKey = mRef.push().getKey();
        if (pushKey == null) throw new IllegalStateException("Key cannot be null!");*/

        mRef.child(String.valueOf(souvenir.getId())).setValue(souvenir, (databaseError, databaseReference) -> {
            if (databaseError != null) Timber.i("DatabaseError: %s", databaseError.getMessage());
            if (databaseReference != null) Timber.i("The databaseReference is: %s", databaseReference.toString());
        });
    }

    public void addSouvenir(SouvenirDb db, DatabaseReference.CompletionListener listener) {
        mRef.child(String.valueOf(db.getId())).setValue(db, (databaseError, databaseReference) -> {
            if (databaseError != null) Timber.i("DatabaseError: %s", databaseError.getMessage());
            if (databaseReference != null) Timber.i("The databaseReference is: %s", databaseReference.toString());
        });
    }

}
