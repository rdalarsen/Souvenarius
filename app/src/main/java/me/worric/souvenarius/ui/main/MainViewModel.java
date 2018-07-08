package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
        mPhotoPath = new MutableLiveData<>();
    }

    public LiveData<List<String>> getFirebaseIds() {
        return Transformations.map(mSouvenirRepository.getSouvenirs(), souvenirs -> {
            if (souvenirs != null && souvenirs.size() > 0) {
                List<String> resultList = new ArrayList<>(souvenirs.size());
                for (SouvenirResponse response : souvenirs) {
                    resultList.add(response.getFirebaseId());
                }
                return resultList;
            }
            Timber.w("Souvenirs do not exist, returning error message");
            return Collections.singletonList("Place not available");
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
