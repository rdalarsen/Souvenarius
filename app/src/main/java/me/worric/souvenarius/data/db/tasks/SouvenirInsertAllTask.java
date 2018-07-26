package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import java.util.Arrays;

import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import timber.log.Timber;


public final class SouvenirInsertAllTask extends AsyncTask<SouvenirDb[],Void,Void> {

    private AppDatabase mAppDatabase;
    private SouvenirDao mDao;

    public SouvenirInsertAllTask(AppDatabase appDatabase) {
        mAppDatabase = appDatabase;
        mDao = appDatabase.souvenirDao();
    }

    @Override
    protected Void doInBackground(SouvenirDb[]... souvenirs) {
        Timber.i("Attempting to DELETE all content in the db and INSERT all elements in array with length: %d", souvenirs[0].length);
        try {
            mAppDatabase.beginTransaction();
            mDao.removeDatabaseContents();
            mDao.insertAll(souvenirs[0]);
            mAppDatabase.setTransactionSuccessful();
            Timber.i("INSERT ALL --- Ids of inserted items are: %s", Arrays.toString(souvenirs[0]));
        } finally {
            mAppDatabase.endTransaction();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

    }

}
