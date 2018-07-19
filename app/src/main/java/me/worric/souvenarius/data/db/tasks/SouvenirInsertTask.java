package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import java.util.Arrays;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import timber.log.Timber;

public final class SouvenirInsertTask extends AsyncTask<SouvenirDb, Void, Void> {

    private SouvenirDao mDao;

    public SouvenirInsertTask(SouvenirDao dao) {
        mDao = dao;
    }

    @Override
    protected Void doInBackground(SouvenirDb... souvenirDbs) {
        Timber.i("The length of the arguments is: %d\ninserting data into the database...", souvenirDbs.length);
        SouvenirDb souvenirDb = souvenirDbs[0];
        long[] ids = mDao.insert(souvenirDb);
        Timber.i("ID of inserted Souvenir: %s", Arrays.toString(ids));
        return null;
    }

}
