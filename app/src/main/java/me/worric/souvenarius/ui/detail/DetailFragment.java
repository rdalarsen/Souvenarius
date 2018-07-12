package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.BR;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentDetailBinding;

public class DetailFragment extends Fragment {

    @Inject
    protected ViewModelProvider.Factory mFactory;
    private DetailViewModel mViewModel;
    private FragmentDetailBinding mBinding;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String souvenirId = getArguments().getString("souvenirId");
        mViewModel = ViewModelProviders.of(this, mFactory).get(DetailViewModel.class);
        mViewModel.setSouvenirId(souvenirId);

        mBinding.setLifecycleOwner(this);
        mBinding.setVariable(BR.viewmodel, mViewModel);
        mBinding.setVariable(BR.clickHandler, (OnClickEdit) v -> {
            TextType textType;
            int viewId = v.getId();
            if (viewId == R.id.tv_detail_title) {
                textType = TextType.TITLE;
            } else if (viewId == R.id.tv_detail_place) {
                textType = TextType.PLACE;
            } else if (viewId == R.id.tv_detail_story) {
                textType = TextType.STORY;
            } else if (viewId == R.id.tv_detail_timestamp) {
                textType = TextType.DATE;
            } else {
                throw new IllegalArgumentException("Unknown view ID: " + viewId);
            }
            EditDialogFragment.newInstance("Edit details", textType)
                    .show(getChildFragmentManager(), "edit_title");
        });
    }

    public static DetailFragment newInstance(String souvenirId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("souvenirId", souvenirId);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnClickEdit {
        void onClickEdit(View view);
    }

    public enum TextType {
        TITLE,
        PLACE,
        DATE,
        STORY
    }

}
