package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.repository.SouvenirRepository;

public class DetailViewModel extends ViewModel {

    private final SouvenirRepository mRepository;
    private final MutableLiveData<Souvenir> mSouvenir;

    @Inject
    public DetailViewModel(SouvenirRepository repository) {
        mSouvenir = new MutableLiveData<>();
        mRepository = repository;
    }

    public void setSouvenir(Souvenir souvenir) {
        mSouvenir.setValue(souvenir);
    }

    public LiveData<Souvenir> getSouvenir() {
        return mSouvenir;
    }

    public void setTitle(String title) {
        Souvenir souvenir = mSouvenir.getValue();
        if (souvenir != null) {
            souvenir.setTitle(title);
            mRepository.updateSouvenir(souvenir);
        }
    }

    public String getTitle() {
        Souvenir souvenir = mSouvenir.getValue();
        if (souvenir != null) {
            return souvenir.getTitle();
        }
        return null;
    }

}
