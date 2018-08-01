package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.text.Editable;

import java.io.File;

import javax.inject.Inject;

import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class DetailViewModel extends ViewModel {

    private final SouvenirRepository mRepository;
    private final MutableLiveData<String> mSouvenirId;
    private final MediatorLiveData<SouvenirDb> mCurrentSouvenir;
    private final MutableLiveData<File> mPhotoFile;

    @Inject
    public DetailViewModel(SouvenirRepository repository) {
        mRepository = repository;
        mSouvenirId = new MutableLiveData<>();
        mPhotoFile = new MutableLiveData<>();
        mCurrentSouvenir = new MediatorLiveData<>();
        LiveData<SouvenirDb> findOne = Transformations.switchMap(mSouvenirId, mRepository::findOneById);
        mCurrentSouvenir.addSource(findOne, souvenirDb -> {
            Timber.i("observer triggered!");
            mCurrentSouvenir.setValue(souvenirDb);
        });
    }

    public void setSouvenirId(String souvenirId) {
        if (mSouvenirId.getValue() == null) {
            mSouvenirId.setValue(souvenirId);
        }
    }

    public LiveData<SouvenirDb> getCurrentSouvenir() {
        return mCurrentSouvenir;
    }

    public void updateSouvenirText(Editable editable, DetailFragment.TextType textType) {
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (souvenir != null) {
            switch (textType) {
                case TITLE:
                    souvenir.setTitle(editable.toString());
                    break;
                case PLACE:
                    souvenir.setPlace(editable.toString());
                    break;
                case STORY:
                    souvenir.setStory(editable.toString());
                    break;
            }
            mRepository.updateSouvenir(souvenir, null);
        }
    }

    public boolean deletePhoto(File photoFile) {
        Timber.i("Delete photo triggered! Photo name was: %s", photoFile != null ? photoFile.getName() : "(PHOTO IS NULL)");
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (souvenir != null) {
            boolean deleteResult = souvenir.getPhotos().remove(photoFile.getName());
            if (deleteResult) {
                mRepository.updateSouvenir(souvenir, null);
                mRepository.deleteFileFromStorage(photoFile.getName());
                if (photoFile.exists()) {
                    Timber.d("photofile exists, deleting...");
                    photoFile.delete();
                } else {
                    Timber.e("PhotoFile did NOT exist; skip delete");
                }
            }
            return deleteResult;
        }
        return false;
    }

    public boolean clearPhoto() {
        File photoFile = mPhotoFile.getValue();
        if (photoFile != null) {
            setCurrentPhotoFile(null);
            if (photoFile.exists()) {
                return photoFile.delete();
            }
        }
        return false;
    }

    public boolean addPhoto() {
        File currentFile = mPhotoFile.getValue();
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (currentFile != null && souvenir != null) {
            boolean addResult = souvenir.getPhotos().add(currentFile.getName());
            if (addResult) {
                mRepository.updateSouvenir(souvenir, currentFile);
            }
            return addResult;
        }
        return false;
    }

    public void setCurrentPhotoFile(File photoFile) {
        mPhotoFile.setValue(photoFile);
    }

    public void deleteSouvenir() {
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (souvenir != null) {
            mRepository.deleteSouvenir(souvenir);
        }
    }

}
