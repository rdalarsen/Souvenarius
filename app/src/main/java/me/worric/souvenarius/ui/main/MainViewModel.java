package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
    private MutableLiveData<MainFragment.ListStyle> mListStyle;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
    }

    public LiveData<MainFragment.ListStyle> getListStyle() {
        if (mListStyle == null) {
            mListStyle = new MutableLiveData<>();
            mListStyle.setValue(MainFragment.ListStyle.LIST);
        }
        return mListStyle;
    }

    public void toggleListStyles() {
        MainFragment.ListStyle style = mListStyle.getValue();
        if (style != null) {
            if (style.equals(MainFragment.ListStyle.LIST)) {
                mListStyle.setValue(MainFragment.ListStyle.STAGGERED);
            } else {
                mListStyle.setValue(MainFragment.ListStyle.LIST);
            }
        }
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
