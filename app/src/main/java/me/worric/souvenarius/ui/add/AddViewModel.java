package me.worric.souvenarius.ui.add;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.repository.SouvenirRepository;

public class AddViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;
    private final MutableLiveData<File> mPhotoPath;

    @Inject
    public AddViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
        mPhotoPath = new MutableLiveData<>();
    }

    public void addSouvenir(SouvenirSaveInfo info) {
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
