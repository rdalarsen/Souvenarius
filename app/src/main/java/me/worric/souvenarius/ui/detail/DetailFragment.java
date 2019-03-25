package me.worric.souvenarius.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;
import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.databinding.FragmentDetailBinding;
import me.worric.souvenarius.ui.common.FileUtils;
import me.worric.souvenarius.ui.common.NetUtils;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static me.worric.souvenarius.ui.common.FileUtils.PHOTO_HEIGHT;
import static me.worric.souvenarius.ui.common.FileUtils.PHOTO_WIDTH;

public class DetailFragment extends Fragment implements
        DeletePhotoConfirmationDialog.PhotoDeletionConfirmedListener,
        DeleteSouvenirConfirmationDialog.SouvenirDeletionConfirmedListener {

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
    private DetailFragmentEventListener mDetailFragmentEventListener;
    @Inject ViewModelProvider.Factory mFactory;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            mDetailFragmentEventListener = (DetailFragmentEventListener) context;
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Attached activity does not implement" +
                    " DetailFragmentEventListener: " + context.toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        String souvenirId = requireArguments().getString(KEY_SOUVENIR_ID);
        mViewModel = ViewModelProviders.of(this, mFactory).get(DetailViewModel.class);
        mViewModel.setSouvenirId(souvenirId);
        mAdapter = new SouvenirPhotoAdapter(mDeletePhotoClickListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        mBinding.setViewmodel(mViewModel);
        mBinding.setClickHandler(mEditClickListener);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.getCurrentSouvenir().observe(getViewLifecycleOwner(), souvenir -> {
            mAdapter.swapPhotos(souvenir);
            mBinding.setCurrentSouvenir(souvenir);
            restoreLayoutManagerState(savedInstanceState);
            restoreScrollViewState(savedInstanceState);
        });

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
        mBinding.rvSouvenirPhotoList.setAdapter(mAdapter);
        mBinding.rvSouvenirPhotoList.setHasFixedSize(true);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mBinding.rvSouvenirPhotoList);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLayoutManagerState = mBinding.rvSouvenirPhotoList.getLayoutManager().onSaveInstanceState();
        mScrollViewPosition = new int[]{mBinding.svDetailRoot.getScrollX(),
                mBinding.svDetailRoot.getScrollY()};
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_detail_add_photo:
                if (!NetUtils.isConnected(requireContext())) {
                    showErrorToast();
                    return true;
                }
                takePhoto();
                return true;
            case R.id.action_detail_delete_souvenir:
                if (!NetUtils.isConnected(requireContext())) {
                    showErrorToast();
                    return true;
                }
                DeleteSouvenirConfirmationDialog.newInstance()
                        .show(getChildFragmentManager(), TAG_DELETE_SOUVENIR);
                return true;
            case R.id.action_detail_share_souvenir:
                startActivity(createShareIntent());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    private Intent createShareIntent() {
        return ShareCompat.IntentBuilder.from(requireActivity())
                .setChooserTitle(R.string.share_dialog_title_detail)
                .setType("text/plain")
                .setText(createTextFromSouvenir())
                .createChooserIntent();
    }

    @NonNull
    private String createTextFromSouvenir() {
        SouvenirDb souvenir = mViewModel.getCurrentSouvenir().getValue();
        if (souvenir != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(souvenir.getTitle());
            sb.append("\n");
            sb.append(souvenir.getPlace());
            sb.append("\n");
            sb.append(souvenir.getStory());

            return sb.toString();
        }
        return "";
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDetailFragmentEventListener = null;
    }

    @Override
    public void onDeleteSouvenirConfirmed() {
        mViewModel.deleteSouvenir(requireContext());
        mDetailFragmentEventListener.onSouvenirDeleted();
    }

    @Override
    public void onDeletePhotoConfirmed(String photoName) {
        File photoFile = FileUtils.getLocalFileForPhotoName(photoName, requireContext());
        if (mViewModel.deletePhoto(photoFile)) {
            Timber.i("Successfully deleted");
        } else {
            Timber.i("Did not get deleted successfully!");
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ((intent.resolveActivity(requireActivity().getPackageManager())) != null) {
            File photo = FileUtils.createTempImageFile(requireContext());
            mViewModel.setPhotoFile(photo);
            if (photo != null) {
                Uri photoUri = FileUtils.getUriForFile(photo, requireContext());
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

    private final SouvenirPhotoAdapter.DeletePhotoClickListener mDeletePhotoClickListener = photoName -> {
        if (!NetUtils.isConnected(requireContext())) {
            showErrorToast();
            return;
        }

        DeletePhotoConfirmationDialog.newInstance(photoName)
                .show(getChildFragmentManager(), TAG_DELETE_PHOTO);
    };

    private final EditClickListener mEditClickListener = view -> {
        if (!NetUtils.isConnected(requireContext())) {
            showErrorToast();
            return;
        }

        TextType textType;
        int viewId = view.getId();
        if (viewId == R.id.tv_detail_edit_title) {
            textType = TextType.TITLE;
        } else if (viewId == R.id.tv_detail_edit_place) {
            textType = TextType.PLACE;
        } else if (viewId == R.id.tv_detail_edit_story) {
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

    public interface DetailFragmentEventListener {
        void onSouvenirDeleted();
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
