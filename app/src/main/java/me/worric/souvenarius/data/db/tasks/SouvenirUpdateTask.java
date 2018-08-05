package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

public final class SouvenirUpdateTask extends AsyncTask<SouvenirDb, Void, Integer> {

    private SouvenirDao mDao;
    private OnDataUpdateListener mListener;

    public SouvenirUpdateTask(SouvenirDao dao, OnDataUpdateListener listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(SouvenirDb... souvenirDbs) {
        SouvenirDb souvenirDb = souvenirDbs[0];
        return mDao.updateOne(souvenirDb);
    }

    @Override
    protected void onPostExecute(Integer numRowsAffected) {
        if (mListener != null) {
            mListener.onDataUpdated(numRowsAffected);
        }
    }

    public interface OnDataUpdateListener {
        void onDataUpdated(int numRowsAffected);
    }

}