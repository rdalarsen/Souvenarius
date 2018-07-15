package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.model.SouvenirResponse;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
    }

    public LiveData<List<Souvenir>> getSouvenirs() {
        return Transformations.map(mSouvenirRepository.getSouvenirs(), souvenirs -> {
            if (souvenirs != null && souvenirs.size() > 0) {
                List<Souvenir> resultList = new ArrayList<>(souvenirs.size());
                for (SouvenirResponse response : souvenirs) {
                    resultList.add(response.toSouvenir());
                }
                return resultList;
            }
            Timber.w("Souvenirs do not exist, returning null");
            return null;
        });
    }

    @Override
    protected void onCleared() {
        Timber.d("onCleared called");
    }

}
