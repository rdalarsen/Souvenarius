package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.di.AppContext;
import timber.log.Timber;

@Singleton
public class LocationRepository {

    private final FusedLocationProviderClient mClient;
    private final Geocoder mGeocoder;
    private MutableLiveData<Result<Address>> mResult;

    @Inject
    public LocationRepository(@AppContext Context context) {
        mClient = new FusedLocationProviderClient(context);
        mGeocoder = new Geocoder(context, Locale.getDefault());
    }

    public LiveData<Result<Address>> getLocation() {
        Timber.i("Getting Location...");
        if (mResult == null) {
            mResult = new MutableLiveData<>();
            fetchLocation();
        }
        return mResult;
    }

    private void fetchLocation() {
        mClient.getLastLocation().addOnSuccessListener(location -> {
            Result<Address> result;
            if (location != null) {
                List<Address> addresses;
                try {
                    addresses = mGeocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);
                    if (addresses != null && addresses.size() > 0) {
                        result = Result.success(addresses.get(0));
                    } else {
                        result = Result.failure("No matching addresses");
                    }
                } catch (IOException e) {
                    Timber.e(e, "getLocation: There was an error fetching the stuff");
                    result = Result.failure(e.getMessage());
                }
            } else {
                 result = Result.failure("No locations found. Is location services disabled?");
            }
            mResult.setValue(result);
        }).addOnFailureListener(e -> {
            Result<Address> result = Result.failure(e.getMessage());
            mResult.setValue(result);
        });
    }

    public void clearResult() {
        mResult = null;
    }

}