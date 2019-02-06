package me.worric.souvenarius.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.databinding.FragmentSearchBinding;
import me.worric.souvenarius.ui.common.FabStateChanger;
import me.worric.souvenarius.ui.main.FabState;
import timber.log.Timber;

public class SearchFragment extends Fragment {

    private static final String ICONIFIED = "iconified";
    private static final String QUERY = "query";
    private FragmentSearchBinding mBinding;
    private SearchViewModel mViewModel;
    private SearchResultsAdapter mAdapter;
    private SearchView mSearchView;
    private boolean isIconified = false;
    private String mQuery;
    private FabStateChanger mFabStateChanger;
    private SearchFragmentEventListener mSearchFragmentEventListener;
    @Inject
    protected ViewModelProvider.Factory mFactory;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            mFabStateChanger = (FabStateChanger) context;
            mSearchFragmentEventListener = (SearchFragmentEventListener) context;
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Attached activity does not implement either" +
                    " FabStateChanger or SearchFragmentEventListener or both: " + context.toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mViewModel = ViewModelProviders.of(this, mFactory).get(SearchViewModel.class);
        mAdapter = new SearchResultsAdapter(souvenir ->
                mSearchFragmentEventListener.onSearchResultClicked(souvenir));
        if (savedInstanceState != null) {
            isIconified = savedInstanceState.getBoolean(ICONIFIED, true);
            mQuery = savedInstanceState.getString(QUERY, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setSearchResultAdapter(mAdapter);
        mBinding.setItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFabStateChanger.changeFabState(FabState.HIDDEN);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSearchView != null) {
            mQuery = mSearchView.getQuery().toString();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        mSearchView = (SearchView) menu.findItem(R.id.action_search_filter).getActionView();
        setupSearchView();
    }

    private void setupSearchView() {
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint(getString(R.string.hint_search_search_for_title));
        mSearchView.setIconified(isIconified);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Timber.i("Search Query triggered: %s", s);
                mViewModel.submitTitleSearchQuery(s);
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Timber.d("Logging queryTextChange: %s", s);
                return false;
            }
        });
        mSearchView.setOnCloseListener(() -> {
            Timber.d("onCloseListener triggered");
            if (TextUtils.isEmpty(mSearchView.getQuery())) {
                hideSoftKeyboard();
                mSearchFragmentEventListener.onClearButtonClicked();
                return true;
            }
            return false;
        });
        mSearchView.setQuery(mQuery, true);

        if (!TextUtils.isEmpty(mQuery)) mSearchView.clearFocus();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_filter:
                // no-op
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSearchView != null) {
            String query = mSearchView.getQuery().toString();
            boolean isIconified = mSearchView.isIconified();
            outState.putBoolean(ICONIFIED, isIconified);
            outState.putString(QUERY, query);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFabStateChanger = null;
        mSearchFragmentEventListener = null;
    }

    public interface SearchFragmentEventListener {
        void onClearButtonClicked();
        void onSearchResultClicked(SouvenirDb souvenir);
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    public SearchFragment() {
    }

}
