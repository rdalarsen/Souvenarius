package me.worric.souvenarius.ui.add;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.location.Address;
import android.support.annotation.NonNull;

import java.io.File;

import javax.inject.Inject;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.location.LocationRepository;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;

public class AddViewModel extends ViewModel {

    private final LocationRepository mLocationRepository;
    private final SouvenirRepository mSouvenirRepository;
    private final MutableLiveData<File> mPhotoFile;

    @Inject
    public AddViewModel(SouvenirRepository souvenirRepository, LocationRepository locationRepository) {
        this(souvenirRepository, locationRepository, new MutableLiveData<>());
    }

    public AddViewModel(SouvenirRepository souvenirRepository, LocationRepository locationRepository,
                        MutableLiveData<File> photoFile) {
        mLocationRepository = locationRepository;
        mSouvenirRepository = souvenirRepository;
        mPhotoFile = photoFile;
    }

    public boolean addSouvenir(@NonNull SouvenirSaveInfo info, @NonNull Context context) {
        File photo = mPhotoFile.getValue();
        if (photo != null) {
            SouvenirDb db = new SouvenirDb();
            db.setTitle(info.getTitle());
            db.setStory(info.getStory());
            db.setPlace(info.getPlace());
            db.getPhotos().add(photo.getName());
            mSouvenirRepository.addSouvenir(db, photo, () ->
                    UpdateWidgetService.startWidgetUpdate(context));
            return true;
        }
        return false;
    }

    public void setPhotoFile(File theFile) {
        mPhotoFile.setValue(theFile);
    }

    public void setPhotoFile(@NonNull String path) {
        File currentPhotoFile = mPhotoFile.getValue();
        if (currentPhotoFile == null) {
            File restoredFile = new File(path);
            mPhotoFile.setValue(restoredFile);
        }
    }

    public LiveData<File> getPhotoFile() {
        return mPhotoFile;
    }

    public boolean deleteTempPhoto() {
        File currentFile = mPhotoFile.getValue();
        if (currentFile != null) {
            mPhotoFile.setValue(null);
            return currentFile.delete();
        }
        return false;
    }

    public LiveData<Result<Address>> getLocationInfo() {
        return mLocationRepository.getLocation();
    }

    @Override
    protected void onCleared() {
        mLocationRepository.clearResult();
    }

}
