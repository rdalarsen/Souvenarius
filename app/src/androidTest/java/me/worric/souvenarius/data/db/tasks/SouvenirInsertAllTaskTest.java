package me.worric.souvenarius.data.db.tasks;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SouvenirInsertAllTaskTest {

    public static final String FAKE_ID = "fakeId";
    public static final String UID = "fakeUid";

    @Rule public final MockitoRule mMockitoRule = MockitoJUnit.rule();

    @Mock private AppDatabase mAppDatabase;
    @Mock private SouvenirDao mSouvenirDao;
    @Mock private SouvenirInsertAllTask.OnDataInsertAllListener mInsertAllListener;

    /**
     * We're making use of a hacky executor that makes commands execute on the current thread instead
     * of a background thread as per normal.
     *
     * See <a href="https://stackoverflow.com/a/28020173/8562738">this SO post</a>.
     */
    private final Executor mSameThreadExecutor = Runnable::run;
    private final SouvenirDb[] mTestSouvenirs;
    private SouvenirInsertAllTask mInsertAllTask;

    public SouvenirInsertAllTaskTest() {
        SouvenirDb souvenir = new SouvenirDb();
        souvenir.setId(FAKE_ID);
        mTestSouvenirs = new SouvenirDb[]{souvenir};
    }

    @Before
    public void setUp() {
        mInsertAllTask = new SouvenirInsertAllTask(mAppDatabase, mSouvenirDao, UID, mInsertAllListener);
    }

    @Test
    public void onInsertAllFailure_doesNotCallListener() {
        when(mSouvenirDao.removeUserSouvenirs(anyString())).thenReturn(1);
        when(mSouvenirDao.insertAll(any(SouvenirDb[].class))).thenReturn(null);

        mInsertAllTask.executeOnExecutor(mSameThreadExecutor, mTestSouvenirs);

        verifyZeroInteractions(mInsertAllListener);
    }

    /**
     * As this test seems particularly flaky, we use the countdown latch approach.
     */
    @Test
    public void onInsertAllSuccess_correctlyCallsListener() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        doAnswer((Answer<Void>) invocation -> {
            signal.countDown();
            verify(mInsertAllListener).onDataInserted();
            return null;
        }).when(mInsertAllListener).onDataInserted();

        when(mSouvenirDao.removeUserSouvenirs(anyString())).thenReturn(1);
        when(mSouvenirDao.insertAll(any(SouvenirDb[].class))).thenReturn(new Long[]{1L});

        mInsertAllTask.executeOnExecutor(mSameThreadExecutor, mTestSouvenirs);

        signal.await();
    }

    @Test
    public void onInsertAll_correctlyInvokesDatabaseTransaction() {
        mInsertAllTask.executeOnExecutor(mSameThreadExecutor, mTestSouvenirs);

        verify(mAppDatabase).beginTransaction();
        verify(mAppDatabase).setTransactionSuccessful();
        verify(mAppDatabase).endTransaction();
    }

}