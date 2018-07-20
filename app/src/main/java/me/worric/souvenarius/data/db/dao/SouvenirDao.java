package me.worric.souvenarius.data.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import me.worric.souvenarius.data.db.model.SouvenirDb;

@Dao
public interface SouvenirDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertAll(SouvenirDb[] souvenirs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insert(SouvenirDb... souvenirDbs);

    @Query("SELECT * FROM souvenirs ORDER BY timestamp ASC")
    LiveData<List<SouvenirDb>> findAllOrderByTimeAsc();

    @Query("SELECT * FROM souvenirs ORDER BY timestamp ASC")
    List<SouvenirDb> findAllOrderByTimeAscSync();

    @Query("SELECT * FROM souvenirs ORDER BY timestamp DESC")
    LiveData<List<SouvenirDb>> findAllOrderByTimeDesc();

    @Query("DELETE FROM souvenirs")
    int removeDatabaseContents();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateOne(SouvenirDb souvenirDb);

    @Query("SELECT * FROM souvenirs WHERE id = :id")
    LiveData<SouvenirDb> findOneById(Long id);

}
