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
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * The camera functionality code in this class is heavily inspired by
 * <a href="https://developer.android.com/training/camera/photobasics">this guide</a> from Google.
 */
public class AddFragment extends Fragment {

    private static final int TAKE_PHOTO_REQUEST_CODE = 909;

    @Inject
    protected ViewModelProvider.Factory mFactory;
    private FragmentAddBinding mBinding;
    private AddViewModel mViewModel;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mFactory).get(AddViewModel.class);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickHandler(mClickHandler);
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
                Toast.makeText(getContext(), "Could allocate temporary file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.i("Photo was taken OK");
            } else {
                boolean wasSuccessfullyDeleted = mViewModel.deleteTempImage();
                Timber.w("Could not take photo. The temp file was %s deleted", (wasSuccessfullyDeleted ? "successfully" : "not"));
            }
        }
    }

    private final ClickHandler mClickHandler = new ClickHandler() {
        @Override
        public void onAddPhotoClicked(View view) {
            takePhoto();
        }

        @Override
        public void onSaveSouvenirClicked(View view) {
            String story = mBinding.etStory.getText().toString();
            String title = mBinding.etSouvenirTitle.getText().toString();
            String place = mBinding.etPlace.getText().toString();
            SouvenirSaveInfo info = new SouvenirSaveInfo(story, title, place);
            if (info.hasMissingValues()) {
                Toast.makeText(getContext(), "There are missing values. Please input them",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (mViewModel.addSouvenir(info)) {
                    Toast.makeText(getContext(), "Souvenir successfully saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Souvenir COULD NOT be saved!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public interface ClickHandler {
        void onAddPhotoClicked(View view);
        void onSaveSouvenirClicked(View view);
    }

    public static AddFragment newInstance() {
        Bundle args = new Bundle();
        AddFragment fragment = new AddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public AddFragment() {
    }

}
