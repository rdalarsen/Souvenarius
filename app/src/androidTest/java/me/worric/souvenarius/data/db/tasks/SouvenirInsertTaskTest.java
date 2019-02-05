package me.worric.souvenarius.data.db.tasks;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SouvenirInsertTaskTest {

    public static final String FAKE_ID = "fakeId";

    @Rule public final MockitoRule mMockitoRule = MockitoJUnit.rule();

    @Mock private SouvenirDao mSouvenirDao;
    @Mock private OnResultListener<SouvenirDb> mOnResultListener;

    @Captor private ArgumentCaptor<SouvenirDb> mSouvenirCaptor;

    @InjectMocks private SouvenirInsertTask mInsertTask;

    /**
     * We're making use of a hacky executor that makes commands execute on the current thread instead
     * of a background thread as per normal.
     *
     * See <a href="https://stackoverflow.com/a/28020173/8562738">this SO post</a>.
     */
    private final Executor mSameThreadExecutor = Runnable::run;
    private final SouvenirDb mTestSouvenir;

    public SouvenirInsertTaskTest() {
        mTestSouvenir = new SouvenirDb();
        mTestSouvenir.setId(FAKE_ID);
    }

    @Test
    public void onDbFailure_correctlyCallsListener() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        doAnswer(invocation -> {
            signal.countDown();
            return null;
        }).when(mOnResultListener).onFailure();
        when(mSouvenirDao.insert(any(SouvenirDb.class))).thenReturn(-1L);

        mInsertTask.executeOnExecutor(mSameThreadExecutor, mTestSouvenir);
        signal.await();

        verify(mOnResultListener).onFailure();
    }

    @Test
    public void onDbSuccess_correctlyCallsListener() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        doAnswer(invocation -> {
            signal.countDown();
            return null;
        }).when(mOnResultListener).onSuccess(any(SouvenirDb.class));
        when(mSouvenirDao.insert(any(SouvenirDb.class))).thenReturn(1L);

        mInsertTask.executeOnExecutor(mSameThreadExecutor, mTestSouvenir);
        signal.await();

        verify(mOnResultListener).onSuccess(mSouvenirCaptor.capture());
        assertThat(mSouvenirCaptor.getValue().getId(), is(equalTo(mTestSouvenir.getId())));
    }

}