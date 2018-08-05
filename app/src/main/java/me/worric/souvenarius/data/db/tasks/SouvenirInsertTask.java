package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;
import timber.log.Timber;

public final class SouvenirInsertTask extends AsyncTask<SouvenirDb, Void, SouvenirDb> {

    private SouvenirDao mDao;
    private OnDataInsertListener mListener;

    public SouvenirInsertTask(SouvenirDao dao, OnDataInsertListener listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected SouvenirDb doInBackground(SouvenirDb... souvenirDbs) {
        Timber.i("The length of the arguments is: %d\nAttemting to INSERT data into the database...", souvenirDbs.length);
        SouvenirDb souvenirDb = souvenirDbs[0];
        mDao.insert(souvenirDb);
        return souvenirDb;
    }

    @Override
    protected void onPostExecute(SouvenirDb souvenirDb) {
        mListener.onDataInserted(souvenirDb);
    }

    public interface OnDataInsertListener {
        void onDataInserted(SouvenirDb souvenirDb);
    }

}
