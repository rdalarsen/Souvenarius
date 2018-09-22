package me.worric.souvenarius.ui.add;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentAddBinding;
import me.worric.souvenarius.ui.common.FileUtils;
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.main.FabState;
import me.worric.souvenarius.ui.main.MainActivity;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static me.worric.souvenarius.ui.common.FileUtils.PHOTO_HEIGHT;
import static me.worric.souvenarius.ui.common.FileUtils.PHOTO_WIDTH;

public class AddFragment extends Fragment {

    private static final int TAKE_PHOTO_REQUEST_CODE = 909;
    private static final String KEY_FILE_PATH = "key_file_path";
    private FragmentAddBinding mBinding;
    private AddViewModel mViewModel;
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
        mViewModel = ViewModelProviders.of(this, mFactory).get(AddViewModel.class);
        restorePhotoFile(savedInstanceState);
    }

    private void restorePhotoFile(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FILE_PATH)) {
            mViewModel.setPhotoFile(savedInstanceState.getString(KEY_FILE_PATH));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add, container, false);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mClickListener);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setFabState(FabState.HIDDEN);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        File currentFile = mViewModel.getPhotoFile().getValue();
        if (currentFile != null) {
            outState.putString(KEY_FILE_PATH, currentFile.getAbsolutePath());
        }
    }

    /**
     * The camera functionality code in this method is heavily inspired by
     * <a href="https://developer.android.com/training/camera/photobasics">this guide</a> from Google.
     */
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
                Toast.makeText(getContext(), R.string.error_message_add_temp_file, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), R.string.error_message_add_no_photo_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorToast() {
        Toast.makeText(getContext(), R.string.error_message_add_no_connection, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File photoFile = mViewModel.getPhotoFile().getValue();
                FileUtils.persistOptimizedBitmapToDisk(photoFile, PHOTO_WIDTH, PHOTO_HEIGHT);
            } else {
                if (mViewModel.deleteTempPhoto()) {
                    Timber.w("Could not take photo. The temp file was successfully deleted");
                }
            }
        }
    }

    private final ClickListener mClickListener = new ClickListener() {
        @Override
        public void onAddPhotoClicked(View view) {
            if (!NetUtils.isConnected(getContext())) {
                showErrorToast();
                return;
            }

            takePhoto();
        }

        @Override
        public void onSaveSouvenirClicked(View view) {
            if (!NetUtils.isConnected(getContext())) {
                showErrorToast();
                return;
            }

            SouvenirSaveInfo info = extractEditTextValuesToSaveInfo();
            if (info.hasMissingValues()) {
                Toast.makeText(getContext(), R.string.error_message_add_missing_values,
                        Toast.LENGTH_SHORT).show();
            } else {
                if (mViewModel.addSouvenir(info, getContext())) {
                    Toast.makeText(getContext(), R.string.success_message_add_souvenir_saved, Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).handleSouvenirSaved();
                } else {
                    Toast.makeText(getContext(), R.string.error_message_souvenir_not_saved, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @NonNull
    private SouvenirSaveInfo extractEditTextValuesToSaveInfo() {
        String story = mBinding.etStory.getText().toString();
        String title = mBinding.etSouvenirTitle.getText().toString();
        String place = mBinding.etPlace.getText().toString();
        
        return new SouvenirSaveInfo(story, title, place);
    }

    public interface ClickListener {
        void onAddPhotoClicked(View view);
        void onSaveSouvenirClicked(View view);
    }

    public static AddFragment newInstance() {
        return new AddFragment();
    }

    public AddFragment() {
    }

}
