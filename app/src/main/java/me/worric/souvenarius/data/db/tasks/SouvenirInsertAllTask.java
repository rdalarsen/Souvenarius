package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;
import timber.log.Timber;


public class SouvenirInsertAllTask extends AsyncTask<SouvenirDb[], Void, Long[]> {

    private final AppDatabase mAppDatabase;
    private final SouvenirDao mDao;
    private final String mUid;
    private final OnDataInsertAllListener mListener;

    public SouvenirInsertAllTask(AppDatabase appDatabase, String uid, OnDataInsertAllListener listener) {
        this(appDatabase, appDatabase.souvenirDao(), uid, listener);
    }

    public SouvenirInsertAllTask(AppDatabase appDatabase, SouvenirDao dao, String uid, OnDataInsertAllListener listener) {
        mAppDatabase = appDatabase;
        mDao = dao;
        mUid = uid;
        mListener = listener;
    }

    @Override
    protected Long[] doInBackground(SouvenirDb[]... souvenirs) {
        Long[] result;
        try {
            mAppDatabase.beginTransaction();
            int numDeletedSouvenirs = mDao.removeUserSouvenirs(mUid);
            result = mDao.insertAll(souvenirs[0]);
            mAppDatabase.setTransactionSuccessful();
            Timber.i("Deleted %d souvenirs from db before inserting %d new",
                    numDeletedSouvenirs, souvenirs[0].length);
        } finally {
            mAppDatabase.endTransaction();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Long[] ids) {
        Timber.d("Current thread (PostExecute): %s", Thread.currentThread().getName());
        if (mListener != null && ids != null && ids.length > 0) {
            mListener.onDataInserted();
        }
    }

    public interface OnDataInsertAllListener {
        void onDataInserted();
    }

}
