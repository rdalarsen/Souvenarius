package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

public final class SouvenirInsertTask extends AsyncTask<SouvenirDb, Void, SouvenirDb> {

    private SouvenirDao mDao;
    private OnDataInsertListener mListener;

    public SouvenirInsertTask(SouvenirDao dao, OnDataInsertListener listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected SouvenirDb doInBackground(SouvenirDb... souvenirDbs) {
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
