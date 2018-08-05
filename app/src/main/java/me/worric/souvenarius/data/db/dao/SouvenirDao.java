package me.worric.souvenarius.data.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;
import android.arch.persistence.room.Update;

import java.util.List;

import me.worric.souvenarius.data.model.SouvenirDb;

@Dao
public interface SouvenirDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(SouvenirDb[] souvenirs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SouvenirDb souvenirDbs);

    @Query("SELECT * FROM souvenirs WHERE uid = :uid ORDER BY timestamp DESC LIMIT 1")
    SouvenirDb findMostRecentSync(String uid);

    @Query("DELETE FROM souvenirs WHERE uid = :uid")
    int removeUserSouvenirs(String uid);

    @Query("DELETE FROM souvenirs WHERE id = :id")
    int deleteSouvenir(String id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateOne(SouvenirDb souvenirDb);

    @Query("SELECT * FROM souvenirs WHERE id = :id")
    LiveData<SouvenirDb> findOneById(String id);

    @RawQuery(observedEntities = SouvenirDb.class)
    LiveData<List<SouvenirDb>> getSouvenirs(SupportSQLiteQuery query);

}
