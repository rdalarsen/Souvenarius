package me.worric.souvenarius.data.repository.location;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.di.LocationErrorMsgs;
import timber.log.Timber;

@Singleton
public class LocationRepositoryImpl implements LocationRepository {

    private static final int MAX_ADDRESS_RESULTS = 1;
    private final Geocoder mGeocoder;
    private final FusedLocationProviderClient mClient;
    private final LocationTaskRunner mLocationTaskRunner;
    private final Map<Integer,String> mErrorMessages;
    private MutableLiveData<Result<Address>> mResult;

    @Inject
    public LocationRepositoryImpl(Geocoder geocoder,
                                  FusedLocationProviderClient client,
                                  LocationTaskRunner locationTaskRunner,
                                  @LocationErrorMsgs Map<Integer,String> errorMessages) {
        mGeocoder = geocoder;
        mClient = client;
        mLocationTaskRunner = locationTaskRunner;
        mErrorMessages = errorMessages;
    }

    @Override
    public LiveData<Result<Address>> getLocation() {
        if (mResult == null) {
            mResult = new MutableLiveData<>();
            fetchLocation();
        }
        return mResult;
    }

    private void fetchLocation() {
        try {
            mClient.getLastLocation().addOnSuccessListener(location -> {
                LocationResultListener listener = result -> mResult.setValue(result);
                mLocationTaskRunner.runGeocodingTask(location, mGeocoder, mErrorMessages, listener);
            }).addOnFailureListener(e -> {
                Result<Address> result = Result.failure(mErrorMessages
                        .get(R.string.error_message_location_repo_no_device_location));
                mResult.setValue(result);
            });
        } catch (SecurityException e) {
            Timber.e(e, "Missing permissions");
            mResult.setValue(Result.failure(mErrorMessages
                    .get(R.string.error_message_location_repo_missing_permission)));
        }
    }

    @Override
    public void clearResult() {
        mResult = null;
    }

    public static class LocationAsyncTask extends AsyncTask<Location, Void, Result<Address>> {

        private final LocationResultListener mListener;
        private final Geocoder mGeocoder;
        private final Map<Integer,String> mErrorMessages;

        LocationAsyncTask(LocationResultListener listener, Geocoder geocoder, Map<Integer,String> errorMessages) {
            mListener = listener;
            mGeocoder = geocoder;
            mErrorMessages = errorMessages;
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
                        result = Result.failure(mErrorMessages
                                .get(R.string.error_message_location_repo_no_matching_address));
                    }
                } catch (IOException e) {
                    Timber.e(e, "Error fetching the location");
                    result = Result.failure(mErrorMessages
                            .get(R.string.error_message_location_repo_error_fetching_location));
                }
            } else {
                result = Result.failure(mErrorMessages
                        .get(R.string.error_message_location_repo_no_device_location));
            }

            return result;
        }

        @Override
        protected void onPostExecute(Result<Address> addressResult) {
            mListener.onLocationResult(addressResult);
        }
    }

    public interface LocationResultListener {
        void onLocationResult(Result<Address> result);
    }

}