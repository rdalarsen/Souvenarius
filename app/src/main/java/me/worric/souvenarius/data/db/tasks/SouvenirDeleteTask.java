package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

public final class SouvenirDeleteTask extends AsyncTask<SouvenirDb, Void, Integer> {

    private SouvenirDao mDao;
    private OnDataDeleteListener mListener;

    public SouvenirDeleteTask(SouvenirDao dao, OnDataDeleteListener listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(SouvenirDb... souvenirDbs) {
        SouvenirDb souvenir = souvenirDbs[0];
        return mDao.deleteSouvenir(souvenir.getId());
    }

    @Override
    protected void onPostExecute(Integer numRowsAffected) {
        if (mListener != null) {
            mListener.onDataDeleted(numRowsAffected);
        }
    }

    public interface OnDataDeleteListener {
        void onDataDeleted(int numRowsAffected);
    }

}
