package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentDetailBinding;
import me.worric.souvenarius.ui.common.FileUtils;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class DetailFragment extends Fragment {

    private static final String KEY_SOUVENIR_ID = "key_souvenir_id";
    private static final int TAKE_PHOTO_REQUEST_CODE = 1009;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        long souvenirId = getArguments().getLong(KEY_SOUVENIR_ID);
        mViewModel = ViewModelProviders.of(this, mFactory).get(DetailViewModel.class);
        mViewModel.setSouvenirId(souvenirId);
        mViewModel.getCurrentSouvenir().observe(this, souvenir -> {
            mAdapter.swapPhotos(souvenir, mBinding.rvSouvenirPhotoList);
            mBinding.setCurrentSouvenir(souvenir);
        });
        mBinding.setLifecycleOwner(this);
        mBinding.setViewmodel(mViewModel);
        mBinding.setClickHandler(mOnEditClickedListener);
        setupRecyclerView();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_detail_add_photo:
                takePhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ((intent.resolveActivity(getContext().getPackageManager())) != null) {
            File photo = FileUtils.createTempImageFile(getContext());
            mViewModel.setCurrentPhotoFile(photo);
            if (photo != null) {
                Uri photoUri = FileUtils.getUriForFile(photo, getContext());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            } else {
                Toast.makeText(getContext(), "Could not allocate temporary file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mViewModel.addPhoto()) {
                    Toast.makeText(getContext(), "Photo successfully added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Photo wasn't added for some reason!", Toast.LENGTH_SHORT).show();
                }
            } else {
                mViewModel.setCurrentPhotoFile(null);
                Toast.makeText(getContext(), "Cancelled taking a photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private PhotoClickListener mPhotoClickListener = photoName -> {
        if (mViewModel.deletePhoto(photoName)) {
            Timber.i("Successfully deleted");
        } else {
            Timber.i("Did not get deleted successfully!");
        }
    };

    public interface PhotoClickListener {
        void onDeletePhoto(String photoName);
    }

    private OnEditClickedListener mOnEditClickedListener = view -> {
        TextType textType;
        int viewId = view.getId();
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
    };

    public interface OnEditClickedListener {
        void onEditClicked(View view);
    }

    public static DetailFragment newInstance(long souvenirId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_SOUVENIR_ID, souvenirId);
        fragment.setArguments(args);
        return fragment;
    }

    public enum TextType {
        TITLE,
        PLACE,
        DATE,
        STORY
    }

    public DetailFragment() {
    }

}
