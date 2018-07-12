package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.BR;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentDetailBinding;

public class DetailFragment extends Fragment {

    private static final String KEY_SOUVENIR_ID = "key_souvenir_id";
    @Inject
    protected ViewModelProvider.Factory mFactory;
    private DetailViewModel mViewModel;
    private FragmentDetailBinding mBinding;
    private SouvenirPhotoAdapter mAdapter;

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
        String souvenirId = getArguments().getString(KEY_SOUVENIR_ID);
        mViewModel = ViewModelProviders.of(this, mFactory).get(DetailViewModel.class);
        mViewModel.setSouvenirId(souvenirId);

        mBinding.setLifecycleOwner(this);
        mBinding.setViewmodel(mViewModel);
        mBinding.setClickHandler(v -> {
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

        setupRecyclerView();

        mViewModel.getCurrentSouvenir().observe(this, souvenir -> {
            mAdapter.swapPhotos(souvenir, mBinding.rvSouvenirPhotoList);
            mBinding.setVariable(BR.currentSouvenir, souvenir);
        });
    }

    private void setupRecyclerView() {
        mBinding.rvSouvenirPhotoList.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new SouvenirPhotoAdapter(mPhotoClickListener);
        mBinding.rvSouvenirPhotoList.setAdapter(mAdapter);
        mBinding.rvSouvenirPhotoList.setHasFixedSize(true);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mBinding.rvSouvenirPhotoList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.getCurrentSouvenir().removeObservers(this);
    }

    public static DetailFragment newInstance(String souvenirId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(KEY_SOUVENIR_ID, souvenirId);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnClickEdit {
        void onClickEdit(View view);
    }

    private PhotoClickListener mPhotoClickListener = new PhotoClickListener() {
        @Override
        public void onDeletePhoto(String photoName) {
            mViewModel.deletePhoto(photoName);
        }

        @Override
        public void onAddPhoto(String photoName) {
            mViewModel.addPhoto(photoName);
        }
    };

    public interface PhotoClickListener {
        void onDeletePhoto(String photoName);

        void onAddPhoto(String photoName);
    }

    public enum TextType {
        TITLE,
        PLACE,
        DATE,
        STORY
    }

}
