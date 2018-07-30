package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.databinding.FragmentMainBinding;
import me.worric.souvenarius.ui.common.PrefsUtils;
import timber.log.Timber;

import static me.worric.souvenarius.data.repository.SouvenirRepository.PREFS_KEY_SORT_STYLE;

public class MainFragment extends Fragment {

    @Inject
    protected ViewModelProvider.Factory mFactory;
    @Inject
    protected SharedPreferences mSharedPreferences;
    private FragmentMainBinding mBinding;
    private MainViewModel mViewModel;
    private SouvenirAdapter mAdapter;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity(), mFactory).get(MainViewModel.class);
        mViewModel.getSouvenirs().observe(this, souvenirResult -> {
            if (souvenirResult.status.equals(Result.Status.SUCCESS)) mAdapter.swapLists(souvenirResult.response);
        });
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickHandler(mClickHandler);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mAdapter = new SouvenirAdapter(mItemClickListener);
        mBinding.rvSouvenirList.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).handleFabState(FabState.ADD);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.getSouvenirs().removeObservers(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_nuke_db:
                mViewModel.nukeDb();
                return true;
            case R.id.action_main_sync:
                mViewModel.sync();
                return true;
            case R.id.action_main_sign_out:
                ((MainActivity)getActivity()).handleSignOut();
                return true;
            case R.id.action_main_toggle_sort:
                SortStyle sortStyle = PrefsUtils.getSortStyleFromPrefs(mSharedPreferences, PREFS_KEY_SORT_STYLE);
                Timber.i("SortStyle is: %s", sortStyle.toString());
                commitToggledSortStyleToPrefs(sortStyle);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void commitToggledSortStyleToPrefs(SortStyle sortStyle) {
        if (sortStyle.equals(SortStyle.DESC)) {
            Toast.makeText(getContext(), R.string.main_toast_sort_asc, Toast.LENGTH_SHORT).show();
            mSharedPreferences.edit()
                    .putString(PREFS_KEY_SORT_STYLE, SortStyle.ASC.toString())
                    .apply();
        } else {
            Toast.makeText(getContext(), R.string.main_toast_sort_desc, Toast.LENGTH_SHORT).show();
            mSharedPreferences.edit()
                    .putString(PREFS_KEY_SORT_STYLE, SortStyle.DESC.toString())
                    .apply();
        }
    }

    private final ItemClickListener mItemClickListener = souvenir ->
            ((MainActivity)getActivity()).handleItemClicked(souvenir);

    public interface ItemClickListener {
        void onItemClicked(SouvenirDb souvenir);
    }

    public final ClickHandler mClickHandler = new ClickHandler() {
        @Override
        public void onAddDataClicked(View view) {
            mViewModel.addNewSouvenir();
        }

        @Override
        public void onAddFabClicked(View view) {
            ((MainActivity)getActivity()).onAddFabClicked(view);
        }
    };

    public interface ClickHandler {
        void onAddDataClicked(View view);
        void onAddFabClicked(View view);
    }

    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
