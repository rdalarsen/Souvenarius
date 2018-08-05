package me.worric.souvenarius.data.db.tasks;

import android.os.AsyncTask;

import java.util.Arrays;

import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;
import timber.log.Timber;


public final class SouvenirInsertAllTask extends AsyncTask<SouvenirDb[],Void,Void> {

    private AppDatabase mAppDatabase;
    private SouvenirDao mDao;
    private String mUid;
    private OnDataInsertedListener mListener;

    public SouvenirInsertAllTask(AppDatabase appDatabase, String uid, OnDataInsertedListener listener) {
        mAppDatabase = appDatabase;
        mDao = appDatabase.souvenirDao();
        mUid = uid;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(SouvenirDb[]... souvenirs) {
        Timber.i("Attempting to DELETE entries with uid=%s from db and INSERT all elements in array with length: %d", mUid, souvenirs[0].length);
        try {
            mAppDatabase.beginTransaction();
            int numDeletedSouvenirs = mDao.removeUserSouvenirs(mUid);
            mDao.insertAll(souvenirs[0]);
            mAppDatabase.setTransactionSuccessful();
            Timber.i("DELETED %d souvenirs --- INSERTED items: Ids of inserted items are: %s", numDeletedSouvenirs, Arrays.toString(souvenirs[0]));
        } finally {
            mAppDatabase.endTransaction();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mListener != null) {
            mListener.onDataInserted();
        }
    }

    public interface OnDataInsertedListener {
        void onDataInserted();
    }

}
