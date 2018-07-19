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
    private final MutableLiveData<Long> mSouvenirId;
    private final MediatorLiveData<SouvenirDb> mCurrentSouvenir;
    private final LiveData<SouvenirDb> mFindOne;
    private final MutableLiveData<File> mPhotoFile;

    @Inject
    public DetailViewModel(SouvenirRepository repository) {
        mRepository = repository;
        mSouvenirId = new MutableLiveData<>();
        mPhotoFile = new MutableLiveData<>();
        mFindOne = Transformations.switchMap(mSouvenirId, mRepository::findOne);
        mCurrentSouvenir = new MediatorLiveData<>();
        mCurrentSouvenir.addSource(mFindOne, souvenirDb -> {
            Timber.i("observer triggered!");
            mCurrentSouvenir.setValue(souvenirDb);
        });
    }

    public void setSouvenirId(long souvenirId) {
        if (mSouvenirId.getValue() == null) {
            mSouvenirId.setValue(souvenirId);
        }
    }

    public LiveData<SouvenirDb> getCurrentSouvenir() {
        Timber.i("getCurrentSouvenir called!");
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
            mRepository.updateSouvenir(souvenir);
        }
    }

    public boolean deletePhoto(String photoName) {
        Timber.i("Delete photo triggered! Photo name was: %s", photoName);
        SouvenirDb souvenir = mCurrentSouvenir.getValue();
        if (souvenir != null && souvenir.getPhotos().size() > 0) {
            boolean deleteResult = souvenir.getPhotos().remove(photoName);
            if (deleteResult) {
                mCurrentSouvenir.setValue(souvenir);
                // TODO: update in repo as well
                //mRepository.updateSouvenir(souvenir);
            }
            return deleteResult;
        }
        return false;
    }

    public boolean addPhoto() {
        File currentFile = mPhotoFile.getValue();
        if (currentFile != null) {
            SouvenirDb souvenir = mCurrentSouvenir.getValue();
            if (souvenir != null && souvenir.getPhotos().size() > 0) {
                boolean addResult = souvenir.getPhotos().add(currentFile.getName());
                if (addResult) {
//                    mCurrentSouvenir.setValue(souvenir);
                    // TODO: update in repo as well
                    mRepository.updateSouvenir(souvenir);
                }
                return addResult;
            }
        }
        return false;
    }

    public void setCurrentPhotoFile(File photoFile) {
        mPhotoFile.setValue(photoFile);
    }

}
