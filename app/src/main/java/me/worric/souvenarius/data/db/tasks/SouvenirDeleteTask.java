package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public final class SouvenirDeleteTask extends AsyncTask<SouvenirDb, Void, Integer> {

    private SouvenirDao mDao;
    private SouvenirRepository.DataDeletedCallback mCallback;

    public SouvenirDeleteTask(SouvenirDao dao, SouvenirRepository.DataDeletedCallback callback) {
        mDao = dao;
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(SouvenirDb... souvenirDbs) {
        Timber.i("Attemting to delete the souvenir...");
        SouvenirDb souvenir = souvenirDbs[0];

        return mDao.deleteSouvenir(souvenir.getId());
    }

    @Override
    protected void onPostExecute(Integer numRowsAffected) {
        mCallback.onDataDeleted(numRowsAffected);
    }

}
