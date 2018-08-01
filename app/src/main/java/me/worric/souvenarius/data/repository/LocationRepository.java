package me.worric.souvenarius.data.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

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

    private static final int MAX_ADDRESS_RESULTS = 1;
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
        try {
            mClient.getLastLocation().addOnSuccessListener(location -> {
                LocationCallback callback = result -> mResult.setValue(result);
                new LocationAsyncTask(callback, mGeocoder).execute(location);
            }).addOnFailureListener(e -> {
                Result<Address> result = Result.failure("Cannot get device location");
                mResult.setValue(result);
            });
        } catch (SecurityException e) {
            Timber.e(e, "Missing permission for fetching location");
            mResult.setValue(Result.failure("Missing permission for fetching location"));
        }
    }

    public void clearResult() {
        mResult = null;
    }

    private static class LocationAsyncTask extends AsyncTask<Location, Void, Result<Address>> {

        private final LocationCallback mCallback;
        private final Geocoder mGeocoder;

        LocationAsyncTask(LocationCallback callback, Geocoder geocoder) {
            mCallback = callback;
            mGeocoder = geocoder;
        }

        @Override
        protected Result<Address> doInBackground(Location... locations) {
            Location location = locations[0];
            Result<Address> result;
            if (location != null) {
                try {
                    List<Address> addresses = mGeocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            MAX_ADDRESS_RESULTS);
                    if (addresses != null && addresses.size() > 0) {
                        result = Result.success(addresses.get(0));
                    } else {
                        result = Result.failure("No matching address");
                    }
                } catch (IOException e) {
                    Timber.e(e, "There was an error fetching the location");
                    result = Result.failure("There was an error fetching the location");
                }
            } else {
                result = Result.failure("Cannot get device location");
            }

            return result;
        }

        @Override
        protected void onPostExecute(Result<Address> addressResult) {
            mCallback.onFetchFinished(addressResult);
        }
    }

    public interface LocationCallback {
        void onFetchFinished(Result<Address> result);
    }

}