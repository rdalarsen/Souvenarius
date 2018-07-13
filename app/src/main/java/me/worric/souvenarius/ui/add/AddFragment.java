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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.threeten.bp.Instant;

import java.io.File;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.databinding.FragmentAddBinding;
import me.worric.souvenarius.di.ActivityContext;
import me.worric.souvenarius.ui.common.FileUtils;
import me.worric.souvenarius.ui.main.MainViewModel;
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
    @Inject
    @ActivityContext
    protected Context mContext;
    private FragmentAddBinding mBinding;
    private MainViewModel mViewModel;

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
        mViewModel = ViewModelProviders.of(getActivity(), mFactory).get(MainViewModel.class);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setAddPhotoHandler(view ->
                takePhoto());
        mBinding.setSaveSouvenirHandler(view -> {
            String story = mBinding.etStory.getText().toString();
            String title = mBinding.etSouvenirTitle.getText().toString();
            String place = mBinding.etPlace.getText().toString();
            SouvenirSaveInfo info = new SouvenirSaveInfo(story, title, place);
            if (info.hasMissingValues()) {
                Toast.makeText(mContext, "There are missing values. Please input them",
                        Toast.LENGTH_SHORT).show();
            } else {
                mViewModel.addSouvenir(info);
            }
        });
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ((intent.resolveActivity(mContext.getPackageManager())) != null) {
            File photo = FileUtils.createTempImageFile(getContext());
            mViewModel.setPhotoPath(photo);
            if (photo != null) {
                Uri photoUri = FileUtils.getUriForFile(photo, getContext());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            } else {
                Toast.makeText(mContext, "Could allocate temporary file", Toast.LENGTH_SHORT).show();
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

    public interface AddPhotoHandler {
        void onAddPhoto(View view);

    }

    public interface SaveSouvenirHandler {
        void onSaveSouvenir(View view);

    }

    public static AddFragment newInstance() {

        Bundle args = new Bundle();

        AddFragment fragment = new AddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public AddFragment() {
    }

    public static class SouvenirSaveInfo {

        private String mStory;
        private String mTitle;
        private String mPlace;

        public SouvenirSaveInfo(String story, String title, String place) {
            mStory = story;
            mTitle = title;
            mPlace = place;
        }

        public boolean hasMissingValues() {
            return TextUtils.isEmpty(mStory) || TextUtils.isEmpty(mTitle) || TextUtils.isEmpty(mPlace);
        }

        public String getPlace() {
            return mPlace;
        }

        public void setPlace(String place) {
            mPlace = place;
        }

        public String getStory() {
            return mStory;
        }

        public void setStory(String story) {
            mStory = story;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public Souvenir toSouvenir(File photo) {
            Souvenir souvenir = new Souvenir();
            souvenir.setPlace(mPlace);
            souvenir.setTimestamp(Instant.now().toEpochMilli());
            souvenir.setTitle(mTitle);
            souvenir.setStory(mStory);
            if (photo != null) {
                souvenir.addPhoto(photo.getName());
            }
            return souvenir;
        }
    }

}
