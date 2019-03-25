package me.worric.souvenarius.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.databinding.FragmentSearchBinding;
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
    private SearchFragmentEventListener mSearchFragmentEventListener;
    @Inject ViewModelProvider.Factory mFactory;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            mSearchFragmentEventListener = (SearchFragmentEventListener) context;
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Attached activity does not implement" +
                    " SearchFragmentEventListener: " + context.toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mViewModel = ViewModelProviders.of(this, mFactory).get(SearchViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setSearchResultAdapter(mAdapter);
        mBinding.setItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        setupToolbar(mBinding.tbSearchSearchToolBar);
        return mBinding.getRoot();
    }

    private void setupToolbar(Toolbar toolBar) {
        toolBar.inflateMenu(R.menu.search_menu);
        mSearchView = (SearchView) toolBar.getMenu().findItem(R.id.action_search_filter).getActionView();
        setupSearchView(mSearchView);
    }

    private void setupSearchView(SearchView searchView) {
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint(getString(R.string.hint_search_search_for_title));
        searchView.setIconified(isIconified);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        searchView.setOnCloseListener(() -> {
            Timber.d("onCloseListener triggered");
            if (TextUtils.isEmpty(mSearchView.getQuery())) {
                hideSoftKeyboard();
                mSearchFragmentEventListener.onClearButtonClicked();
                return true;
            }
            return false;
        });
        searchView.setQuery(mQuery, true);

        if (!TextUtils.isEmpty(mQuery)) mSearchView.clearFocus();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSearchView != null) {
            mQuery = mSearchView.getQuery().toString();
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
        mSearchFragmentEventListener = null;
    }

    public interface SearchFragmentEventListener {
        void onClearButtonClicked();
        void onSearchResultClicked(SouvenirDb souvenir);
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    public SearchFragment() {}

}
