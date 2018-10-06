package me.worric.souvenarius.data.repository.location;

import android.arch.lifecycle.LiveData;
import android.location.Address;

import me.worric.souvenarius.data.Result;

public interface LocationRepository {

   LiveData<Result<Address>> getLocation();

   void clearResult();

}
