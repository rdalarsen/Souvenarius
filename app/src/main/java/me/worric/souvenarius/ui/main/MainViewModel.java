package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
    }

    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
        return mSouvenirRepository.getSortedSouvenirs();
    }

    public void nukeDb() {
        mSouvenirRepository.nukeDb();
    }

    public void sync() {
        mSouvenirRepository.refreshSouvenirsFromRemote();
    }

}
