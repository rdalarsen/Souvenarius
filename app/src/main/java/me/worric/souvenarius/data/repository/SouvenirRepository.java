package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;

import java.io.File;
import java.util.List;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.ui.main.SortStyle;

public interface SouvenirRepository {

    LiveData<Result<List<SouvenirDb>>> getSouvenirs();

    LiveData<SouvenirDb> findSouvenirById(String souvenirId);

    void addSouvenir(SouvenirDb db, File photo, OnAddSuccessListener successListener);

    void updateSouvenir(SouvenirDb souvenir, File photo);

    void deleteSouvenir(SouvenirDb souvenir, OnDeleteSuccessListener successListener);

    void deletePhotoFromStorage(String photoName);

    void setQueryParameters(String uid);

    void setQueryParameters(SortStyle sortStyle);

    interface OnDeleteSuccessListener {
        void onSuccessfulDelete();
    }

    interface OnAddSuccessListener {
        void onSuccessfulAdd();
    }

}
