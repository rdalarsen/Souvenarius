package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import java.util.Arrays;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import timber.log.Timber;


public final class SouvenirInsertAllTask extends AsyncTask<SouvenirDb[],Void,Long[]> {

    private SouvenirDao mDao;

    public SouvenirInsertAllTask(SouvenirDao dao) {
        mDao = dao;
    }

    @Override
    protected Long[] doInBackground(SouvenirDb[]... souvenirs) {
        Timber.i("Attempting to INSERT all elements in array with length: %d", souvenirs[0].length);
        return mDao.insertAll(souvenirs[0]);
    }

    @Override
    protected void onPostExecute(Long[] ids) {
        Timber.i("INSERT ALL --- Ids of inserted items are: %s", Arrays.toString(ids));
    }

}
