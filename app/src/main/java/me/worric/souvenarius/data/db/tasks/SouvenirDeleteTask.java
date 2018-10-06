package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;
import android.support.v4.util.Pair;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

public class SouvenirDeleteTask extends AsyncTask<SouvenirDb, Void, Pair<SouvenirDb,Integer>> {

    private final SouvenirDao mDao;
    private final OnResultListener<SouvenirDb> mListener;

    public SouvenirDeleteTask(SouvenirDao dao, OnResultListener<SouvenirDb> listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected Pair<SouvenirDb,Integer> doInBackground(SouvenirDb... souvenirDbs) {
        SouvenirDb souvenir = souvenirDbs[0];
        Integer result = mDao.deleteSouvenir(souvenir.getId());
        return Pair.create(souvenir, result);
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
