package me.worric.souvenarius.ui.add;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.repository.LocationRepository;
import me.worric.souvenarius.data.repository.SouvenirRepository;

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

    public LiveData<String> getLocationInfo() {
        return Transformations.map(mLocationRepository.getLocation(), result -> {
            if (result.status.equals(Result.Status.SUCCESS)) {
                return String.format("%s, %s", result.response.getLocality(),
                        result.response.getCountryName());
            } else if (result.status.equals(Result.Status.FAILURE)) {
                return result.message;
            }
            throw new IllegalArgumentException("Unknown status: " + result.status.name());
        });
    }

    @Override
    protected void onCleared() {
        mLocationRepository.clearResult();
    }
}
