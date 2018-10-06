package me.worric.souvenarius.data.repository.location;

import android.location.Geocoder;
import android.location.Location;

import java.util.Map;

import me.worric.souvenarius.data.repository.location.LocationRepositoryImpl;

public interface LocationTaskRunner {

    void runGeocodingTask(Location location,
                          Geocoder geocoder,
                          Map<Integer,String> errorMessages,
                          LocationRepositoryImpl.LocationResultListener listener);

}
