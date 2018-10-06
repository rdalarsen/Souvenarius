package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;
import android.support.v4.util.Pair;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

public class SouvenirUpdateTask extends AsyncTask<SouvenirDb, Void, Pair<SouvenirDb,Integer>> {

    private final SouvenirDao mDao;
    private final OnResultListener<SouvenirDb> mListener;

    public SouvenirUpdateTask(SouvenirDao dao, OnResultListener<SouvenirDb> listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected Pair<SouvenirDb,Integer> doInBackground(SouvenirDb... souvenirDbs) {
        SouvenirDb souvenirDb = souvenirDbs[0];
        Integer result = mDao.updateOne(souvenirDb);
        return Pair.create(souvenirDb, result);
    }

    @Override
    protected void onPostExecute(Pair<SouvenirDb,Integer> result) {
        if (mListener != null) {
            if (result.first != null && result.second != null && result.second > 0) {
                mListener.onSuccess(result.first);
            } else {
                mListener.onFailure();
            }
        }
    }

}