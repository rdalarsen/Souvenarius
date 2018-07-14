package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

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
    private final MutableLiveData<Result<Address>> mResult;
    private final Geocoder mGeocoder;

    @Inject
    public LocationRepository(@AppContext Context context) {
        mClient = new FusedLocationProviderClient(context);
        mResult = new MutableLiveData<>();
        mGeocoder = new Geocoder(context, Locale.getDefault());
    }

    public LiveData<Result<Address>> getLocation() {
        mClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                Result<Address> result = Result.failure("No locations found. Is location services disabled?");
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
                }
                mResult.setValue(result);
            }
        });
        return mResult;
    }

    public void clearResult() {
        mResult.setValue(null);
    }

}
