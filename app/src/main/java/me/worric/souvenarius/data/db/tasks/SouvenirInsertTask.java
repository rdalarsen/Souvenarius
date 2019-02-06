package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;
import androidx.core.util.Pair;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

public class SouvenirInsertTask extends AsyncTask<SouvenirDb, Void, Pair<SouvenirDb,Long>> {

    private final SouvenirDao mDao;
    private final OnResultListener<SouvenirDb> mListener;

    public SouvenirInsertTask(SouvenirDao dao, OnResultListener<SouvenirDb> listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected Pair<SouvenirDb,Long> doInBackground(SouvenirDb... souvenirDbs) {
        SouvenirDb souvenirDb = souvenirDbs[0];
        Long id = mDao.insert(souvenirDb);
        return Pair.create(souvenirDb, id);
    }

    @Override
    protected void onPostExecute(Pair<SouvenirDb,Long> result) {
        if (mListener != null) {
            if (result.first != null && result.second != null && result.second > -1L) {
                mListener.onSuccess(result.first);
            } else {
                mListener.onFailure();
            }
        }
    }

}
