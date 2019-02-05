package me.worric.souvenarius.ui.detail;

import android.content.Context;
import android.text.Editable;

import java.io.File;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;
import timber.log.Timber;

public class DetailViewModel extends ViewModel {

    private final SouvenirRepository mRepository;
    private final MutableLiveData<String> mSouvenirId;
    private final MediatorLiveData<SouvenirDb> mCurrentSouvenir;
    private final MutableLiveData<File> mPhotoFile;

    @Inject
    public DetailViewModel(SouvenirRepository repository) {
        this(repository, new MutableLiveData<>(), new MediatorLiveData<>(), new MutableLiveData<>());
    }

    public DetailViewModel(SouvenirRepository repository, MutableLiveData<String> souvenirId, MediatorLiveData<SouvenirDb> currentSouvenir, MutableLiveData<File> photoFile) {
        mRepository = repository;
        mSouvenirId = souvenirId;
        mCurrentSouvenir = currentSouvenir;
        mPhotoFile = photoFile;
        LiveData<SouvenirDb> findOne = Transformations.switchMap(mSouvenirId, mRepository::findSouvenirById);
        mCurrentSouvenir.addSource(findOne, mCurrentSouvenir::setValue);
    }

    public void setSouvenirId(String souvenirId) {
        if (mSouvenirId.getValue() == null) {
            mSouvenirId.setValue(souvenirId);
        }
    }

    public void setPhotoFile(File photoFile) {
        mPhotoFile.setValue(photoFile);
    }

    public LiveData<File> getPhotoFile() {
        return mPhotoFile;
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
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (souvenir != null) {
            boolean isRemoved = souvenir.getPhotos().remove(photoFile.getName());
            if (isRemoved) {
                mRepository.updateSouvenir(souvenir, null);
                mRepository.deletePhotoFromStorage(photoFile.getName());
                if (photoFile.exists()) {
                    Timber.d("photoFile exists, deleting...");
                    photoFile.delete();
                } else {
                    Timber.e("PhotoFile did NOT exist; skip delete");
                }
            }
            return isRemoved;
        }
        return false;
    }

    public void clearPhoto() {
        File photoFile = mPhotoFile.getValue();
        if (photoFile != null) {
            setPhotoFile(null);
            if (photoFile.exists()) {
                photoFile.delete();
            }
        }
    }

    public boolean addPhoto() {
        File photoFile = mPhotoFile.getValue();
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (photoFile != null && souvenir != null) {
            boolean isAdded = souvenir.getPhotos().add(photoFile.getName());
            if (isAdded) {
                mRepository.updateSouvenir(souvenir, photoFile);
            }
            return isAdded;
        }
        return false;
    }

    public void deleteSouvenir(@NonNull Context context) {
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (souvenir != null) {
            mRepository.deleteSouvenir(souvenir, () ->
                    UpdateWidgetService.startWidgetUpdate(context));
        }
    }

}
