package me.worric.souvenarius.ui.add;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class AddViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;
    private final MutableLiveData<File> mPhotoFile;

    @Inject
    public AddViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
        mPhotoFile = new MutableLiveData<>();
    }

    public boolean addSouvenir(SouvenirSaveInfo info) {
        File photo = mPhotoFile.getValue();
        if (photo != null) {
            Souvenir souvenir = info.toSouvenir(photo);
            mSouvenirRepository.addSouvenir(souvenir, photo);
            return true;
        }
        return false;
    }

    public void setPhotoFile(File theFile) {
        mPhotoFile.setValue(theFile);
    }

    public LiveData<File> getPhotoFile() {
        return mPhotoFile;
    }

    public boolean deleteTempImage() {
        boolean wasDeletedSuccessfully = Objects.requireNonNull(mPhotoFile.getValue()).delete();
        if (wasDeletedSuccessfully) {
            mPhotoFile.setValue(null);
        }
        return wasDeletedSuccessfully;
    }

    @Override
    protected void onCleared() {
        Timber.d("onCleared called");
    }
}
