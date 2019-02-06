package me.worric.souvenarius.data.repository.souvenir;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.ui.main.SortStyle;

public class FakeSouvenirRepo implements SouvenirRepository {

    private MutableLiveData<Result<List<SouvenirDb>>> mSouvenirLiveData;

    public FakeSouvenirRepo() {
        mSouvenirLiveData = new MutableLiveData<>();
    }

    @Override
    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
        return null;
    }

    @Override
    public LiveData<SouvenirDb> findSouvenirById(String souvenirId) {
        return null;
    }

    @Override
    public LiveData<Result<List<SouvenirDb>>> findSouvenirsByTitle(String title) {
        mSouvenirLiveData.setValue(Result.success(getMockTitleData(title)));
        return mSouvenirLiveData;
    }

    @Override
    public void addSouvenir(SouvenirDb db, File photo, OnAddSuccessListener successListener) {

    }

    @Override
    public void updateSouvenir(SouvenirDb souvenir, File photo) {

    }

    @Override
    public void deleteSouvenir(SouvenirDb souvenir, OnDeleteSuccessListener successListener) {

    }

    @Override
    public void deletePhotoFromStorage(String photoName) {

    }

    @Override
    public void setQueryParameters(String uid) {

    }

    @Override
    public void setQueryParameters(SortStyle sortStyle) {

    }

    private List<SouvenirDb> getMockTitleData(String query) {
        final SouvenirDb testSouvenir1 = new SouvenirDb();
        final SouvenirDb testSouvenir2 = new SouvenirDb();
        testSouvenir1.setId("testId1");
        testSouvenir1.setTitle(query + "test1");
        testSouvenir2.setId("testId2");
        testSouvenir2.setTitle(query + "test2");

        return Arrays.asList(testSouvenir1, testSouvenir2);
    }

}
