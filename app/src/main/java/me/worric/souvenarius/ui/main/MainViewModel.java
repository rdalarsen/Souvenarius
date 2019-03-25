package me.worric.souvenarius.ui.main;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;
    private final LiveData<Integer> mNumSouvenirs;
    private final LiveData<Result<List<SouvenirDb>>> mSouvenirs;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
        mSouvenirs = mSouvenirRepository.getSouvenirs();
        mNumSouvenirs = Transformations.map(mSouvenirs, result -> {
            if (result != null && result.status.equals(Result.Status.SUCCESS)) {
                return result.response.size();
            }
            return null;
        });
    }

    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
        return mSouvenirs;
    }

    public LiveData<Integer> getNumSouvenirs() {
        return mNumSouvenirs;
    }

    public void updateUserId(@Nullable String uid) {
        mSouvenirRepository.setQueryParameters(uid);
    }

    public void updateSortStyle(@NonNull SortStyle sortStyle) {
        mSouvenirRepository.setQueryParameters(sortStyle);
    }

}
