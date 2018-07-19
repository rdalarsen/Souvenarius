package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public final class SouvenirUpdateTask extends AsyncTask<SouvenirDb, Void, Integer> {

    private SouvenirDao mDao;
    private SouvenirRepository.DataUpdateCallback mListener;

    public SouvenirUpdateTask(SouvenirDao dao, SouvenirRepository.DataUpdateCallback listener) {
        mDao = dao;
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(SouvenirDb... souvenirDbs) {
        Timber.i("The length of the arguments is: %d\ninserting data into the database...", souvenirDbs.length);
        SouvenirDb souvenirDb = souvenirDbs[0];
        int numRowsAffected = mDao.updateOne(souvenirDb);
        if (numRowsAffected > 0) {
            Timber.i("Souvenir was updated successfully: %d", numRowsAffected);
            return numRowsAffected;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer numRowsAffected) {
        mListener.onDataUpdated(numRowsAffected);
    }

}