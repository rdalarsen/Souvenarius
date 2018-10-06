package me.worric.souvenarius.ui.search;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.FakeSouvenirRepo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchViewModelTest {

    @Rule public InstantTaskExecutorRule mExecutorRule = new InstantTaskExecutorRule();

    @Mock private LifecycleOwner mLifecycleOwner;
    @Mock private Observer<Result<List<SouvenirDb>>> mResultObserver;

    @Spy private FakeSouvenirRepo mSouvenirRepo;
    @Spy private MutableLiveData<String> mTitleTrigger;

    @Captor private ArgumentCaptor<String> mStringCaptor;
    @Captor private ArgumentCaptor<Result<List<SouvenirDb>>> mResultCaptor;

    @InjectMocks private SearchViewModel mViewModel;

    @Test
    public void getSearchResults_returnsNonNullLiveDataContainingNullValue() {
        LiveData<Result<List<SouvenirDb>>> results = mViewModel.getSouvenirSearchResults();

        assertThat(results, is(notNullValue()));
        assertThat(results.getValue(), is(nullValue()));
    }

    @Test
    public void submitSearchQuery_correctlyTriggersObserverWhileObservingSearchResults() {
        final Lifecycle lifecycle = getPreparedLifecycle(mLifecycleOwner);
        final String titleSearchQuery = "1234";

        when(mLifecycleOwner.getLifecycle()).thenReturn(lifecycle);

        mViewModel.getSouvenirSearchResults().observe(mLifecycleOwner, mResultObserver);
        mViewModel.submitTitleSearchQuery(titleSearchQuery);

        verify(mLifecycleOwner, atLeastOnce()).getLifecycle();
        verify(mResultObserver).onChanged(mResultCaptor.capture());
        assertThat(mResultCaptor.getValue().response, is(notNullValue()));
        assertThat(mResultCaptor.getValue().response.size(), is(equalTo(2)));
        assertThat(mResultCaptor.getValue().response.get(0).getTitle(),
                is(equalTo(titleSearchQuery + "test1")));
        assertThat(mResultCaptor.getValue().response.get(1).getTitle(),
                is(equalTo(titleSearchQuery + "test2")));
    }

    @Test
    public void submitSearchQuery_correctlySetsValueInTrigger() {
        final String titleSearchQuery = "4321";

        mViewModel.submitTitleSearchQuery(titleSearchQuery);

        verify(mTitleTrigger).setValue(mStringCaptor.capture());
        assertThat(mStringCaptor.getValue(), is(equalTo(titleSearchQuery)));
    }

    @Test
    public void submitSearchQuery_correctlyHandlesDuplicateQuery() {
        final String titleSearchQuery = "coffee";

        mViewModel.submitTitleSearchQuery(titleSearchQuery);
        mViewModel.submitTitleSearchQuery(titleSearchQuery);

        verify(mTitleTrigger, times(1)).setValue(anyString());
    }

    private Lifecycle getPreparedLifecycle(LifecycleOwner lifecycleOwner) {
        LifecycleRegistry registry = new LifecycleRegistry(lifecycleOwner);
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        return registry;
    }

}