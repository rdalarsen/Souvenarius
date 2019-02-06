package me.worric.souvenarius.data.repository.location;

import android.location.Address;

import androidx.lifecycle.LiveData;
import me.worric.souvenarius.data.Result;

public interface LocationRepository {

   LiveData<Result<Address>> getLocation();

   void clearResult();

}
