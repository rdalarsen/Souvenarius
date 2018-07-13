package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.text.Editable;

import java.io.File;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.model.SouvenirResponse;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class DetailViewModel extends ViewModel {

    private final SouvenirRepository mRepository;
    private final MutableLiveData<String> mSouvenirId;
    private final MutableLiveData<Souvenir> mUpdatedSouvenir;
    private final MediatorLiveData<Souvenir> mCurrentSouvenir;
    private final MutableLiveData<File> mPhotoFile;

    @Inject
    public DetailViewModel(SouvenirRepository repository) {
        mRepository = repository;
        mSouvenirId = new MutableLiveData<>();
        mUpdatedSouvenir = new MutableLiveData<>();
        mPhotoFile = new MutableLiveData<>();
        mCurrentSouvenir = new MediatorLiveData<>();
        initCurrentSouvenir();
    }

    private void initCurrentSouvenir() {
        LiveData<Souvenir> filterSouvenirsById =
                Transformations.switchMap(mSouvenirId, id ->
                        Transformations.map(mRepository.getSouvenirs(), souvenirResponses -> {
                            for (SouvenirResponse response : souvenirResponses) {
                                if (response.getFirebaseId().equals(id)) {
                                    return response.toSouvenir();
                                }
                            }
                            return null;
                        }));
        mCurrentSouvenir.addSource(filterSouvenirsById, mCurrentSouvenir::setValue);
    }

    public void setSouvenirId(String souvenirId) {
        mSouvenirId.setValue(souvenirId);
    }

    public LiveData<Souvenir> getCurrentSouvenir() {
        return mCurrentSouvenir;
    }

    public void updateSouvenirText(Editable editable, DetailFragment.TextType textType) {
        Souvenir souvenir = mCurrentSouvenir.getValue();
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
            mCurrentSouvenir.setValue(souvenir);
        }
    }

    public boolean deletePhoto(String photoName) {
        Timber.i("Delete photo triggered! Photo name was: %s", photoName);
        Souvenir souvenir = mCurrentSouvenir.getValue();
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
            Souvenir souvenir = mCurrentSouvenir.getValue();
            if (souvenir != null && souvenir.getPhotos().size() > 0) {
                boolean addResult = souvenir.getPhotos().add(currentFile.getName());
                if (addResult) {
                    mCurrentSouvenir.setValue(souvenir);
                    // TODO: update in repo as well
                    //mRepository.updateSouvenir(souvenir);
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
