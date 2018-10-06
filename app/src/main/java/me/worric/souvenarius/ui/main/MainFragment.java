package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.UpdateSouvenirsService;
import me.worric.souvenarius.databinding.FragmentMainBinding;
import me.worric.souvenarius.ui.common.FabStateChanger;
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.common.PrefsUtils;

import static me.worric.souvenarius.ui.common.PrefsUtils.PREFS_KEY_SORT_STYLE;

public class MainFragment extends Fragment {

    private static final String KEY_LAYOUT_MANAGER_STATE = "key_layout_manager_state";
    private FragmentMainBinding mBinding;
    private MainViewModel mViewModel;
    private SouvenirAdapter mAdapter;
    private boolean mShouldRestoreLayoutManagerState = true;
    private Parcelable mLayoutManagerState;
    private FabStateChanger mFabStateChanger;
    private MainFragmentEventListener mMainFragmentEventListener;
    @Inject
    protected ViewModelProvider.Factory mFactory;
    @Inject
    protected SharedPreferences mSharedPreferences;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            mFabStateChanger = (FabStateChanger) context;
            mMainFragmentEventListener = (MainFragmentEventListener) context;
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Attached activity does not implement either" +
                    " FabStateChanger or MainFragmentEventListener or both: " + context.toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mViewModel = ViewModelProviders.of(getActivity(), mFactory).get(MainViewModel.class);
        mAdapter = new SouvenirAdapter(souvenir ->
                mMainFragmentEventListener.onSouvenirClicked(souvenir));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.getSouvenirs().observe(getViewLifecycleOwner(), souvenirResult -> {
            mBinding.setResultSouvenirs(souvenirResult);
            if (souvenirResult.status.equals(Result.Status.SUCCESS)) {
                mAdapter.swapSouvenirs(souvenirResult.response);
                restoreLayoutManagerState(savedInstanceState);
            }
        });
        setupRecyclerView();
    }

    private void restoreLayoutManagerState(@Nullable Bundle savedInstanceState) {
        if (mShouldRestoreLayoutManagerState) {
            if (mLayoutManagerState != null) {
                mBinding.rvSouvenirList.getLayoutManager().onRestoreInstanceState(mLayoutManagerState);
            } else if (savedInstanceState != null && savedInstanceState.containsKey(KEY_LAYOUT_MANAGER_STATE)) {
                mBinding.rvSouvenirList.getLayoutManager().onRestoreInstanceState(savedInstanceState
                        .getParcelable(KEY_LAYOUT_MANAGER_STATE));
            }
        }
        mShouldRestoreLayoutManagerState = true;
    }

    private void setupRecyclerView() {
        mBinding.rvSouvenirList.setAdapter(mAdapter);
        mBinding.rvSouvenirList.setHasFixedSize(true);
        mBinding.srlRefresh.setOnRefreshListener(() -> {
            mShouldRestoreLayoutManagerState = false;
            refreshData();
            mBinding.srlRefresh.setRefreshing(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mFabStateChanger.changeFabState(FabState.ADD);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLayoutManagerState = mBinding.rvSouvenirList.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLayoutManagerState != null) {
            outState.putParcelable(KEY_LAYOUT_MANAGER_STATE, mLayoutManagerState);
        } else if (mBinding != null) {
            outState.putParcelable(KEY_LAYOUT_MANAGER_STATE, mBinding.rvSouvenirList.getLayoutManager()
                    .onSaveInstanceState());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_refresh_data:
                refreshData();
                return true;
            case R.id.action_main_sign_out:
                mMainFragmentEventListener.onSignOutClicked();
                return true;
            case R.id.action_main_toggle_sort:
                SortStyle sortStyle = PrefsUtils.getSortStyleFromPrefs(mSharedPreferences,
                        PREFS_KEY_SORT_STYLE);
                mShouldRestoreLayoutManagerState = false;
                toggleAndPropagateSortStyle(sortStyle);
                return true;
            case R.id.action_main_search:
                mMainFragmentEventListener.onSearchClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshData() {
        if (!NetUtils.isConnected(getContext())) {
            showErrorToast();
        }
        UpdateSouvenirsService.startSouvenirsUpdate(getContext());
    }

    private void showErrorToast() {
        Toast.makeText(getContext(), R.string.error_message_main_no_connection, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFabStateChanger = null;
        mMainFragmentEventListener = null;
    }

    private void toggleAndPropagateSortStyle(SortStyle sortStyle) {
        if (sortStyle.equals(SortStyle.DESC)) {
            Toast.makeText(getContext(), R.string.main_toast_sort_asc, Toast.LENGTH_SHORT).show();
            mSharedPreferences.edit()
                    .putString(PREFS_KEY_SORT_STYLE, SortStyle.ASC.toString())
                    .apply();
            mViewModel.updateSortStyle(SortStyle.ASC);
        } else {
            Toast.makeText(getContext(), R.string.main_toast_sort_desc, Toast.LENGTH_SHORT).show();
            mSharedPreferences.edit()
                    .putString(PREFS_KEY_SORT_STYLE, SortStyle.DESC.toString())
                    .apply();
            mViewModel.updateSortStyle(SortStyle.DESC);
        }
    }

    public interface MainFragmentEventListener {
        void onSouvenirClicked(SouvenirDb souvenir);
        void onSignOutClicked();
        void onSearchClicked();
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {
    }

}
