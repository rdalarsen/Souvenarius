package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import timber.log.Timber;

public final class NukeDbTask extends AsyncTask<Void, Void, Void> {

    private final SouvenirDao mDao;

    public NukeDbTask(SouvenirDao dao) {
        mDao = dao;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int numRowsDeleted = mDao.removeDatabaseContents();
        Timber.e("Database nuked! Numner of affected rows: %d", numRowsDeleted);
        return null;
    }

}
