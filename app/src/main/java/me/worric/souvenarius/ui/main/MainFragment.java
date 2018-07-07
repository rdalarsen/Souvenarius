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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    @Inject
    protected ViewModelProvider.Factory mFactory;
    private FragmentMainBinding mBinding;
    private MainViewModel mViewModel;

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
        mBinding.rvSouvenirList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mBinding.rvSouvenirList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mBinding.rvSouvenirList.setAdapter(new RecyclerView.Adapter<SouvenirViewholder>() {
            @NonNull
            @Override
            public SouvenirViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new SouvenirViewholder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull SouvenirViewholder holder, int position) {
                holder.text1.setText("test");
                holder.text2.setText("and more test");
            }

            @Override
            public int getItemCount() {
                return 30;
            }
        });
    }

    public static class SouvenirViewholder extends RecyclerView.ViewHolder {

        TextView text1;
        TextView text2;

        public SouvenirViewholder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.tv_item_1);
            text2 = itemView.findViewById(R.id.tv_item_2);
        }

    }

    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
