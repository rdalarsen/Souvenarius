package me.worric.souvenarius.ui.add;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Address;
import android.support.annotation.NonNull;

import java.io.File;

import javax.inject.Inject;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.LocationRepository;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class AddViewModel extends ViewModel {

    private final LocationRepository mLocationRepository;
    private final SouvenirRepository mSouvenirRepository;
    private final MutableLiveData<File> mPhotoFile;

    @Inject
    public AddViewModel(SouvenirRepository souvenirRepository, LocationRepository locationRepository) {
        mSouvenirRepository = souvenirRepository;
        mLocationRepository = locationRepository;
        mPhotoFile = new MutableLiveData<>();
    }

    public boolean addSouvenir(SouvenirSaveInfo info) {
        File photo = mPhotoFile.getValue();
        if (photo != null) {
            SouvenirDb db = new SouvenirDb();
            db.setTitle(info.getTitle());
            db.setStory(info.getStory());
            db.setPlace(info.getPlace());
            db.getPhotos().add(photo.getName());
            mSouvenirRepository.addNewSouvenir(db, photo);
            return true;
        }
        return false;
    }

    public void setPhotoFile(File theFile) {
        mPhotoFile.setValue(theFile);
    }

    public void setPhotoFile(@NonNull String path) {
        File currentPhotoFile = mPhotoFile.getValue();
        Timber.i("currentFile: path=%s", currentPhotoFile != null ? currentPhotoFile.getAbsoluteFile() : "(WAS NULL)");
        if (currentPhotoFile == null) {
            Timber.i("Restoring file...");
            File restoredFile = new File(path);
            mPhotoFile.setValue(restoredFile);
        }
    }

    public LiveData<File> getPhotoFile() {
        return mPhotoFile;
    }

    public boolean deleteTempImage() {
        File currentFile = mPhotoFile.getValue();
        if (currentFile != null) {
            mPhotoFile.setValue(null);
            return currentFile.delete();
        }
        return false;
    }

    public LiveData<Result<Address>> getLocationInfo() {
        Timber.i("getLocationInfo called");
        return mLocationRepository.getLocation();
    }

    @Override
    protected void onCleared() {
        Timber.i("Clearing result data...");
        mLocationRepository.clearResult();
    }

}
