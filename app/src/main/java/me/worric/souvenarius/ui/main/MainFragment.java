package me.worric.souvenarius.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
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
    @Inject ViewModelProvider.Factory mFactory;
    @Inject SharedPreferences mSharedPreferences;

    @Override
    public void onAttach(@NonNull Context context) {
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
        mViewModel = ViewModelProviders.of(requireActivity(), mFactory).get(MainViewModel.class);
        mAdapter = new SouvenirAdapter(souvenir ->
                mMainFragmentEventListener.onSouvenirClicked(souvenir));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mClickListener);
        configureToolbar(mBinding.tbMainHeaderToolbar);
        setupRecyclerView();
        return mBinding.getRoot();
    }

    private void configureToolbar(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_main_sign_out) {
                mMainFragmentEventListener.onSignOutClicked();
                return true;
            }
            return false;
        });
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.getSouvenirs().observe(getViewLifecycleOwner(), souvenirResult -> {
            mBinding.setResultSouvenirs(souvenirResult); // TODO: integrate more tightly with data binding
            if (souvenirResult.status.equals(Result.Status.SUCCESS)) {
                mAdapter.swapSouvenirs(souvenirResult.response);
                restoreLayoutManagerState(savedInstanceState);
            }
        });
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

    private void refreshData() {
        if (!NetUtils.isConnected(requireContext())) {
            showErrorToast();
        }
        UpdateSouvenirsService.startSouvenirsUpdate(requireContext());
    }

    private void showErrorToast() {
        Toast.makeText(requireContext(), R.string.error_message_main_no_connection, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFabStateChanger = null;
        mMainFragmentEventListener = null;
    }

    private void toggleAndPropagateSortStyle(SortStyle sortStyle) {
        if (sortStyle.equals(SortStyle.DESC)) {
            Toast.makeText(requireContext(), R.string.main_toast_sort_asc, Toast.LENGTH_SHORT).show();
            mSharedPreferences.edit()
                    .putString(PREFS_KEY_SORT_STYLE, SortStyle.ASC.toString())
                    .apply();
            mViewModel.updateSortStyle(SortStyle.ASC);
        } else {
            Toast.makeText(requireContext(), R.string.main_toast_sort_desc, Toast.LENGTH_SHORT).show();
            mSharedPreferences.edit()
                    .putString(PREFS_KEY_SORT_STYLE, SortStyle.DESC.toString())
                    .apply();
            mViewModel.updateSortStyle(SortStyle.DESC);
        }
    }

    private final ClickListener mClickListener = new ClickListener() {
        @Override
        public void onSortClick(View view) {
            SortStyle sortStyle = PrefsUtils.getSortStyleFromPrefs(mSharedPreferences,
                    PREFS_KEY_SORT_STYLE);
            mShouldRestoreLayoutManagerState = false;
            toggleAndPropagateSortStyle(sortStyle);
        }

        @Override
        public void onSearchClick(View view) {
            mMainFragmentEventListener.onSearchClicked();
        }
    };

    public interface ClickListener {
        void onSortClick(View view);
        void onSearchClick(View view);
    }

    public interface MainFragmentEventListener {
        void onSouvenirClicked(SouvenirDb souvenir);
        void onSignOutClicked();
        void onSearchClicked();
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {}

}
