package me.worric.souvenarius.ui.add;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.text.Editable;

import org.threeten.bp.Instant;

import java.io.File;
import java.util.Objects;

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
    private final MediatorLiveData<String> mLocation;
    private final LiveData<String> mResultAddress;

    @Inject
    public AddViewModel(SouvenirRepository souvenirRepository, LocationRepository locationRepository) {
        mSouvenirRepository = souvenirRepository;
        mLocationRepository = locationRepository;
        mPhotoFile = new MutableLiveData<>();
        mLocation = new MediatorLiveData<>();
        mResultAddress = getLiveData();
        mLocation.addSource(mResultAddress, mLocation::setValue);
    }

    @NonNull
    private LiveData<String> getLiveData() {
        return Transformations.map(mLocationRepository.getLocation(), result -> {
            Timber.i("Result status is: %s", result.status.name());
            if (result.status.equals(Result.Status.SUCCESS)) {
                return String.format("%s, %s", result.response.getLocality(),
                        result.response.getCountryName());
            } else if (result.status.equals(Result.Status.FAILURE)) {
                return result.message;
            }
            throw new IllegalArgumentException("Unknown status: " + result.status.name());
        });
    }

    public void setText(Editable editable) {
        mLocation.setValue(editable.toString());
    }

    public boolean addSouvenir(SouvenirSaveInfo info) {
        File photo = mPhotoFile.getValue();
        if (photo != null) {
            SouvenirDb db = new SouvenirDb();
            db.setTitle(info.getTitle());
            db.setStory(info.getStory());
            db.setPlace(info.getPlace());
            db.setTimestamp(Instant.now().toEpochMilli());
            db.getPhotos().add(photo.getName());
            mSouvenirRepository.addNewSouvenir(db, photo);
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
        return mLocation;
    }

    @Override
    protected void onCleared() {
        Timber.i("Clearing result data...");
        mLocationRepository.clearResult();
    }
}
