package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import java.util.List;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;

public final class SouvenirFetchTask extends AsyncTask<Void, Void, List<SouvenirDb>> {

    private SouvenirDao mDao;
    private SouvenirRepository.DataInsertCallback mListener;

    public SouvenirFetchTask(SouvenirDao dao, SouvenirRepository.DataInsertCallback listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected List<SouvenirDb> doInBackground(Void... voids) {
        return mDao.findAllOrderByTimeAscSync();
    }

    @Override
    protected void onPostExecute(List<SouvenirDb> souvenirDbs) {
        //mListener.onDataInserted(souvenirDbs);
        mListener = null;
    }
}
