package me.worric.souvenarius.data.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import me.worric.souvenarius.data.db.model.SouvenirDb;

@Dao
public interface SouvenirDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SouvenirDb> souvenirs);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insertAll(SouvenirDb... souvenirDbs);

    @Query("SELECT * FROM souvenirs ORDER BY timestamp ASC")
    LiveData<List<SouvenirDb>> findAllOrderByTimeAsc();

}
