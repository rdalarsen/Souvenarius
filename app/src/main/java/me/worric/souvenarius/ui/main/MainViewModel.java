package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
    }

    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
        return mSouvenirRepository.getSouvenirs();
    }

    public void updateUserId(String uid) {
        mSouvenirRepository.setQueryParameters(uid);
    }

    public void updateSortStyle(SortStyle sortStyle) {
        mSouvenirRepository.setQueryParameters(sortStyle);
    }

}
