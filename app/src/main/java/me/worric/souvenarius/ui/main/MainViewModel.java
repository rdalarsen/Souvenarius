package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.model.SouvenirResponse;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import me.worric.souvenarius.ui.add.AddFragment;
import timber.log.Timber;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;
    private final MutableLiveData<File> mPhotoPath;
    private MutableLiveData<MainFragment.ListStyle> mListStyle;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
        mPhotoPath = new MutableLiveData<>();
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

    public void addSouvenir(AddFragment.SouvenirSaveInfo info) {
        File photo = Objects.requireNonNull(mPhotoPath.getValue());
        Souvenir souvenir = info.toSouvenir(photo);
        mSouvenirRepository.addSouvenir(souvenir, photo);
    }

    public void setPhotoPath(File theFile) {
        mPhotoPath.setValue(theFile);
    }

    public LiveData<File> getPhotoPath() {
        return mPhotoPath;
    }

    public boolean deleteTempImage() {
        boolean wasDeletedSuccessfully = Objects.requireNonNull(mPhotoPath.getValue()).delete();
        if (wasDeletedSuccessfully) {
            mPhotoPath.setValue(null);
        }
        return wasDeletedSuccessfully;
    }
}
