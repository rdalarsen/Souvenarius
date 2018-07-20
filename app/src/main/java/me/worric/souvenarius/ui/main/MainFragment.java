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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import timber.log.Timber;

public class MainFragment extends Fragment {

    private static final int ITEM_DECORATION_INDEX = 0;
    private static final String KEY_LIST_STYLE = "key_list_style";
    private static final String KEY_SORT_STYLE = "key_sort_style";
    @Inject
    protected ViewModelProvider.Factory mFactory;
    @Inject
    protected SharedPreferences mSharedPreferences;
    private FragmentMainBinding mBinding;
    private MainViewModel mViewModel;
    private SouvenirAdapter mAdapter;
    private ListStyle mListStyle;
    private SortStyle mSortStyle;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        restoreValues(savedInstanceState);
    }

    private void restoreValues(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mSortStyle = SortStyle.DATE_DESC;
            mListStyle = ListStyle.LIST;
        } else {
            if (savedInstanceState.containsKey(KEY_LIST_STYLE)) {
                mListStyle = (ListStyle) savedInstanceState.getSerializable(KEY_LIST_STYLE);
            }
            if (savedInstanceState.containsKey(KEY_SORT_STYLE)) {
                mSortStyle = (SortStyle) savedInstanceState.getSerializable(KEY_SORT_STYLE);
            }
        }
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
        mViewModel.getSortedSouvenirDbs().observe(this, souvenirs -> {
            if (souvenirs.status.equals(Result.Status.SUCCESS)) mAdapter.swapLists(souvenirs.response);
        });
        mViewModel.getSortStyle().observe(this, sortStyle -> mSortStyle = sortStyle);
        mViewModel.setSortStyle(mSortStyle);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickHandler(mClickHandler);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mAdapter = new SouvenirAdapter(mItemClickListener);
        mBinding.rvSouvenirList.setAdapter(mAdapter);
        setupLayoutManager();
    }

    private void setupLayoutManager() {
        RecyclerView.LayoutManager manager;
        if (mListStyle == null || mListStyle.equals(ListStyle.LIST)) {
            manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            mBinding.rvSouvenirList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        } else if (mListStyle.equals(ListStyle.STAGGERED)) {
            manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            if (mBinding.rvSouvenirList.getItemDecorationCount() > 0) {
                mBinding.rvSouvenirList.removeItemDecorationAt(ITEM_DECORATION_INDEX);
            }
        } else {
            throw new IllegalArgumentException("Unknown ListStyle: " + mListStyle.toString());
        }
        mBinding.rvSouvenirList.setLayoutManager(manager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.getSortedSouvenirDbs().removeObservers(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_LIST_STYLE, mListStyle);
        outState.putSerializable(KEY_SORT_STYLE, mSortStyle);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final ItemClickListener mItemClickListener = souvenir -> {
        try {
            ((MainActivity)getActivity()).handleItemClicked(souvenir);
        } catch (ClassCastException e) {
            Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    };

    public interface ItemClickListener {
        void onItemClicked(SouvenirDb souvenir);
    }

    public final ClickHandler mClickHandler = new ClickHandler() {
        @Override
        public void onToggleLayoutClicked(View view) {
            RecyclerView.LayoutManager manager;
            if (mListStyle.equals(ListStyle.LIST)) {
                // Load staggered + toggle list style
                if (mBinding.rvSouvenirList.getItemDecorationCount() > 0) {
                    mBinding.rvSouvenirList.removeItemDecorationAt(ITEM_DECORATION_INDEX);
                }
                manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mListStyle = ListStyle.STAGGERED;
            } else {
                // Load list + toggle list style
                mBinding.rvSouvenirList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                mListStyle = ListStyle.LIST;
            }
            mBinding.rvSouvenirList.setLayoutManager(manager);
        }

        @Override
        public void onToggleSortClicked(View view) {
            SortStyle sortStyle = getSortStyleFromPrefs();
            Timber.i("SortStyle is: %s", sortStyle.toString());
            if (sortStyle.equals(SortStyle.DATE_DESC)) {
                mSharedPreferences.edit()
                        .putString("sortStyle", SortStyle.DATE_ASC.toString())
                        .apply();
            } else {
                mSharedPreferences.edit()
                        .putString("sortStyle", SortStyle.DATE_DESC.toString())
                        .apply();
            }
        }

        @Override
        public void onAddDataClicked(View view) {
            mViewModel.addNewSouvenir();
        }
    };

    @NonNull
    private SortStyle getSortStyleFromPrefs() {
        String value = mSharedPreferences.getString("sortStyle", SortStyle.DATE_DESC.toString());
        return SortStyle.valueOf(value);
    }

    public interface ClickHandler {
        void onToggleLayoutClicked(View view);
        void onToggleSortClicked(View view);
        void onAddDataClicked(View view);
    }

    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public enum ListStyle {
        LIST,
        STAGGERED
    }

}
