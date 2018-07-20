package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import java.util.Arrays;

import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import timber.log.Timber;


public final class SouvenirInsertAllTask extends AsyncTask<SouvenirDb[],Void,Long[]> {

    private AppDatabase mAppDatabase;
    private SouvenirDao mDao;

    public SouvenirInsertAllTask(AppDatabase appDatabase) {
        mAppDatabase = appDatabase;
        mDao = appDatabase.souvenirDao();
    }

    @Override
    protected Long[] doInBackground(SouvenirDb[]... souvenirs) {
        Timber.i("Attempting to DELETE all content in the db and INSERT all elements in array with length: %d", souvenirs[0].length);
        Long[] ids;
        try {
            mAppDatabase.beginTransaction();
            mDao.removeDatabaseContents();
            ids = mDao.insertAll(souvenirs[0]);
            mAppDatabase.setTransactionSuccessful();
        } finally {
            mAppDatabase.endTransaction();
        }
        return ids != null ? ids : new Long[]{};
    }

    @Override
    protected void onPostExecute(Long[] ids) {
        Timber.i("INSERT ALL --- Ids of inserted items are: %s", Arrays.toString(ids));
    }

}
