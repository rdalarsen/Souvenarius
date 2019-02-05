package me.worric.souvenarius.data.db.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;
import me.worric.souvenarius.data.model.SouvenirDb;

@Dao
public interface SouvenirDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertAll(SouvenirDb[] souvenirs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(SouvenirDb souvenirDbs);

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

    @Query("SELECT * FROM souvenirs WHERE uid = :uid AND title LIKE :title")
    LiveData<List<SouvenirDb>> findSouvenirsByTitle(String uid, String title);

}
