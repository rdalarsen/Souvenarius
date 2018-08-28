package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
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
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.main.FabState;
import me.worric.souvenarius.ui.main.MainActivity;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static me.worric.souvenarius.ui.common.FileUtils.PHOTO_HEIGHT;
import static me.worric.souvenarius.ui.common.FileUtils.PHOTO_WIDTH;

public class DetailFragment extends Fragment {

    private static final String KEY_LAYOUT_MANAGER_STATE = "key_layout_manager_state";
    private static final String KEY_SCROLL_POSITION = "key_scroll_position";
    private static final String KEY_SOUVENIR_ID = "key_souvenir_id";
    private static final String TAG_EDIT_DETAIL = "edit_detail";
    private static final String TAG_DELETE_PHOTO = "delete_photo";
    private static final String TAG_DELETE_SOUVENIR = "delete_dialog";
    private static final int TAKE_PHOTO_REQUEST_CODE = 1009;
    private DetailViewModel mViewModel;
    private FragmentDetailBinding mBinding;
    private SouvenirPhotoAdapter mAdapter;
    private Parcelable mLayoutManagerState;
    private int[] mScrollViewPosition;
    @Inject
    protected ViewModelProvider.Factory mFactory;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String souvenirId = getArguments().getString(KEY_SOUVENIR_ID);
        mViewModel = ViewModelProviders.of(this, mFactory).get(DetailViewModel.class);
        mViewModel.setSouvenirId(souvenirId);
        mViewModel.getCurrentSouvenir().observe(this, souvenir -> {
            mAdapter.swapPhotos(souvenir);
            mBinding.setCurrentSouvenir(souvenir);
            restoreLayoutManagerState(savedInstanceState);
            restoreScrollViewState(savedInstanceState);
        });
        mBinding.setLifecycleOwner(this);
        mBinding.setViewmodel(mViewModel);
        mBinding.setClickHandler(mEditClickListener);
        setupRecyclerView();
    }

    private void restoreLayoutManagerState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_LAYOUT_MANAGER_STATE)) {
            Parcelable savedLayoutManagerState = savedInstanceState
                    .getParcelable(KEY_LAYOUT_MANAGER_STATE);
            mBinding.rvSouvenirPhotoList.getLayoutManager()
                    .onRestoreInstanceState(savedLayoutManagerState);
        }
    }

    private void restoreScrollViewState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SCROLL_POSITION)) {
            int[] scrollPosition = savedInstanceState.getIntArray(KEY_SCROLL_POSITION);
            mBinding.svDetailRoot.post(() ->
                    mBinding.svDetailRoot.scrollTo(scrollPosition[0],scrollPosition[1]));
        }
    }

    private void setupRecyclerView() {
        mBinding.rvSouvenirPhotoList.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new SouvenirPhotoAdapter(mDeletePhotoClickListener);
        mBinding.rvSouvenirPhotoList.setAdapter(mAdapter);
        mBinding.rvSouvenirPhotoList.setHasFixedSize(true);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mBinding.rvSouvenirPhotoList);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setFabState(FabState.HIDDEN);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLayoutManagerState = mBinding.rvSouvenirPhotoList.getLayoutManager().onSaveInstanceState();
        mScrollViewPosition = new int[]{mBinding.svDetailRoot.getScrollX(),
                mBinding.svDetailRoot.getScrollY()};
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.getCurrentSouvenir().removeObservers(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLayoutManagerState != null) {
            outState.putParcelable(KEY_LAYOUT_MANAGER_STATE, mLayoutManagerState);
        }
        if (mScrollViewPosition != null) {
            outState.putIntArray(KEY_SCROLL_POSITION, mScrollViewPosition);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_detail_add_photo:
                if (!NetUtils.isConnected(getContext())) {
                    showErrorToast();
                    return true;
                }
                takePhoto();
                return true;
            case R.id.action_detail_delete_souvenir:
                if (!NetUtils.isConnected(getContext())) {
                    showErrorToast();
                    return true;
                }
                DeleteSouvenirConfirmationDialog.newInstance()
                        .show(getChildFragmentManager(), TAG_DELETE_SOUVENIR);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onDeleteSouvenirConfirmed() {
        mViewModel.deleteSouvenir(getContext());
        ((MainActivity) getActivity()).handleSouvenirDeleted();
    }

    public void onDeletePhotoConfirmed(String photoName) {
        File photoFile = FileUtils.getLocalFileForPhotoName(photoName, getContext());
        if (mViewModel.deletePhoto(photoFile)) {
            Timber.i("Successfully deleted");
        } else {
            Timber.i("Did not get deleted successfully!");
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ((intent.resolveActivity(getContext().getPackageManager())) != null) {
            File photo = FileUtils.createTempImageFile(getContext());
            mViewModel.setPhotoFile(photo);
            if (photo != null) {
                Uri photoUri = FileUtils.getUriForFile(photo, getContext());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            } else {
                Toast.makeText(getContext(), R.string.error_message_detail_fail_temp_file_allocation, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), R.string.error_message_add_no_photo_app, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File photoFile = mViewModel.getPhotoFile().getValue();
                FileUtils.persistOptimizedBitmapToDisk(photoFile, PHOTO_WIDTH, PHOTO_HEIGHT);
                if (mViewModel.addPhoto()) {
                    Toast.makeText(getContext(), R.string.success_message_detail_photo_added, Toast.LENGTH_SHORT).show();
                } else {
                    mViewModel.clearPhoto();
                    Toast.makeText(getContext(), R.string.error_message_detail_photo_not_added, Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                mViewModel.clearPhoto();
                Toast.makeText(getContext(), R.string.error_message_detail_photo_cancelled, Toast.LENGTH_SHORT).show();
            } else {
                throw new IllegalArgumentException("Unknown result code: " + resultCode);
            }
        }
    }

    private void showErrorToast() {
        Toast.makeText(getContext(), R.string.error_message_detail_no_connection, Toast.LENGTH_SHORT).show();
    }

    private DeletePhotoClickListener mDeletePhotoClickListener = photoName -> {
        if (!NetUtils.isConnected(getContext())) {
            showErrorToast();
            return;
        }

        DeletePhotoConfirmationDialog.newInstance(photoName)
                .show(getChildFragmentManager(), TAG_DELETE_PHOTO);
    };

    public interface DeletePhotoClickListener {
        void onDeletePhotoClicked(String photoName);
    }

    private EditClickListener mEditClickListener = view -> {
        if (!NetUtils.isConnected(getContext())) {
            showErrorToast();
            return;
        }

        TextType textType;
        int viewId = view.getId();
        if (viewId == R.id.tv_detail_label_title) {
            textType = TextType.TITLE;
        } else if (viewId == R.id.tv_detail_label_place) {
            textType = TextType.PLACE;
        } else if (viewId == R.id.tv_detail_label_story) {
            textType = TextType.STORY;
        } else {
            throw new IllegalArgumentException("Unknown view ID: " + viewId);
        }

        EditDialog.newInstance(textType)
                .show(getChildFragmentManager(), TAG_EDIT_DETAIL);
    };

    public interface EditClickListener {
        void onEditClicked(View view);
    }

    public static DetailFragment newInstance(String souvenirId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(KEY_SOUVENIR_ID, souvenirId);
        fragment.setArguments(args);
        return fragment;
    }

    public enum TextType {
        TITLE,
        PLACE,
        STORY
    }

    public DetailFragment() {
    }

}
