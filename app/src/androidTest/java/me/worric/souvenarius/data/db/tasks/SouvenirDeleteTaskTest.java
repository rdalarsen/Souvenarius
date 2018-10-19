package me.worric.souvenarius.data.db.tasks;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.Executor;

import me.worric.souvenarius.data.db.dao.SouvenirDao;
import me.worric.souvenarius.data.model.SouvenirDb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SouvenirDeleteTaskTest {

    public static final String FAKE_ID = "fakeId";

    @Rule public final MockitoRule mMockitoRule = MockitoJUnit.rule();

    @Mock private SouvenirDao mSouvenirDao;
    @Mock private OnResultListener<SouvenirDb> mOnResultListener;

    @Captor private ArgumentCaptor<SouvenirDb> mSouvenirCaptor;

    @InjectMocks private SouvenirDeleteTask mDeleteTask;

    /**
     * We're making use of a hacky executor that makes commands execute on the current thread instead
     * of a background thread as per normal.
     *
     * See <a href="https://stackoverflow.com/a/28020173/8562738">this SO post</a>.
     */
    private final Executor mSameThreadExecutor = Runnable::run;
    private final SouvenirDb mTestSouvenir;

    public SouvenirDeleteTaskTest() {
        mTestSouvenir = new SouvenirDb();
        mTestSouvenir.setId(FAKE_ID);
    }

    @Test
    public void onDbFailure_correctlyCallsListener() {
        when(mSouvenirDao.deleteSouvenir(anyString())).thenReturn(0);

        mDeleteTask.executeOnExecutor(mSameThreadExecutor, mTestSouvenir);

        verify(mSouvenirDao).deleteSouvenir(anyString());
        verify(mOnResultListener).onFailure();
    }

    @Test
    public void onDbSuccess_correctlyCallsListener() {
        when(mSouvenirDao.deleteSouvenir(anyString())).thenReturn(1);

        mDeleteTask.executeOnExecutor(mSameThreadExecutor, mTestSouvenir);

        verify(mSouvenirDao).deleteSouvenir(anyString());
        verify(mOnResultListener).onSuccess(mSouvenirCaptor.capture());
        assertThat(mSouvenirCaptor.getValue().getId(), is(equalTo(mTestSouvenir.getId())));
    }

}