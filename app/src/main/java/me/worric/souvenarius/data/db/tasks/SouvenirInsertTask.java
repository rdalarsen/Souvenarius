package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import java.util.Arrays;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public final class SouvenirInsertTask extends AsyncTask<SouvenirDb, Void, SouvenirDb> {

    private SouvenirDao mDao;
    private SouvenirRepository.DataInsertCallback mListener;

    public SouvenirInsertTask(SouvenirDao dao, SouvenirRepository.DataInsertCallback listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected SouvenirDb doInBackground(SouvenirDb... souvenirDbs) {
        Timber.i("The length of the arguments is: %d\nAttemting to INSERT data into the database...", souvenirDbs.length);
        SouvenirDb souvenirDb = souvenirDbs[0];
        long[] ids = mDao.insert(souvenirDb);
        if (ids[0] != -1) {
            Timber.i("ID of inserted Souvenir: %s", Arrays.toString(ids));
            souvenirDb.setId(ids[0]);
            return souvenirDb;
        }
        return null;
    }

    @Override
    protected void onPostExecute(SouvenirDb souvenirDb) {
        mListener.onDataInserted(souvenirDb);
    }

}
