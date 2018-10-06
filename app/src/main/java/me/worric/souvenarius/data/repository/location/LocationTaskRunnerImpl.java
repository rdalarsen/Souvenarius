package me.worric.souvenarius.data.repository.location;

import android.location.Geocoder;
import android.location.Location;

import java.util.Map;

import javax.inject.Inject;

public class LocationTaskRunnerImpl implements LocationTaskRunner {

    @Inject
    public LocationTaskRunnerImpl() {
    }

    @Override
    public void runGeocodingTask(Location location,
                                 Geocoder geocoder,
                                 Map<Integer, String> errorMessages,
                                 LocationRepositoryImpl.LocationResultListener listener) {
        new LocationRepositoryImpl.LocationAsyncTask(listener, geocoder, errorMessages).execute(location);
    }



}
