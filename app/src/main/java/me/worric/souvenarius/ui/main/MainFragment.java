package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private static final int ITEM_DECORATION_INDEX = 0;
    private static final String KEY_LIST_STYLE = "key_list_style";
    @Inject
    protected ViewModelProvider.Factory mFactory;
    private FragmentMainBinding mBinding;
    private MainViewModel mViewModel;
    private SouvenirAdapter mAdapter;
    private ListStyle mListStyle = ListStyle.LIST;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_LIST_STYLE)) {
            mListStyle = (ListStyle) savedInstanceState.getSerializable(KEY_LIST_STYLE);
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
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickHandler(mClickHandler);
        setupRecyclerView();
        mViewModel.getSouvenirs().observe(this, strings -> mAdapter.swapLists(strings));
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
        mViewModel.getSouvenirs().removeObservers(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_LIST_STYLE, mListStyle);
    }

    private final ItemClickListener mItemClickListener = souvenir -> {
        try {
            ((MainActivity)getActivity()).handleItemClicked(souvenir);
        } catch (ClassCastException e) {
            Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    };

    public interface ItemClickListener {
        void onItemClicked(Souvenir souvenir);
    }

    private final ClickHandler mClickHandler = view -> {
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
    };

    public interface ClickHandler {
        void onToggleLayoutClicked(View view);
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

    public enum SortStyle {
        LOCATION_ASC,
        LOCATION_DESC,
        DATE_ASC,
        DATE_DESC
    }

}
