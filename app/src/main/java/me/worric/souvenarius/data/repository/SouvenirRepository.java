package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;

import java.io.File;
import java.util.Collections;
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
        return Transformations.map(mFirebaseHandler.getResults(), result -> {
            if (Status.SUCCESS.equals(result.status)) {
                return result.response;
            } else if (Status.FAILURE.equals(result.status)) {
                return Collections.emptyList();
            }
            throw new IllegalArgumentException("Unknown result");
        });
    }

    public void addSouvenir(Souvenir souvenir, File image) {
        mStorageHandler.uploadImage(image);
        mFirebaseHandler.storeSouvenir(souvenir);
    }

    public static final class Result<T> {

        public final T response;
        public final Status status;
        public final Exception e;

        private Result(T response, Status status, Exception e) {
            this.response = response;
            this.status = status;
            this.e = e;
        }

        public static <T> Result<T> success(T response) {
            return new Result<>(response, Status.SUCCESS, null);
        }

        public static <T> Result<T> failure(Exception e) {
            return new Result<>(null, Status.FAILURE, e);
        }
    }

    public enum Status {
        SUCCESS,
        FAILURE
    }

}
