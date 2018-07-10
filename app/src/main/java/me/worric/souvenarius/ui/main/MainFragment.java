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

import javax.inject.Inject;

import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private static final int ITEM_DECORATION_INDEX = 0;
    @Inject
    protected ViewModelProvider.Factory mFactory;
    private FragmentMainBinding mBinding;
    private MainViewModel mViewModel;
    private SouvenirAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        mViewModel = ViewModelProviders.of(getActivity(), mFactory).get(MainViewModel.class);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new SouvenirAdapter();
        mBinding.rvSouvenirList.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.getSouvenirs().observe(this, strings -> mAdapter.swapLists(strings));
        mViewModel.getListStyle().observe(this, listStyle -> {
            RecyclerView.LayoutManager manager;
            if (listStyle == null || listStyle.equals(ListStyle.LIST)) {
                manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                mBinding.rvSouvenirList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            } else if (listStyle.equals(ListStyle.STAGGERED)) {
                manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mBinding.rvSouvenirList.removeItemDecorationAt(ITEM_DECORATION_INDEX);
            } else {
                throw new IllegalArgumentException("Unknown ListStyle: " + listStyle.toString());
            }
            mBinding.rvSouvenirList.setLayoutManager(manager);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.getSouvenirs().removeObservers(this);
        mViewModel.getListStyle().removeObservers(this);
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
