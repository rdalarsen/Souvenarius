package me.worric.souvenarius.ui.add;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.BR;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.databinding.FragmentAddBinding;
import me.worric.souvenarius.di.ActivityContext;
import me.worric.souvenarius.ui.main.MainViewModel;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class AddFragment extends Fragment {

    private static final int TAKE_PHOTO_REQUEST_CODE = 909;
    private static final String DATE_PATTERN = "yyyyMMdd_HHmmss";
    private static final String FILE_NAME_PREFIX = "JPEG_";
    private static final String FILE_NAME_SEPARATOR = "_";
    private static final String FILE_NAME_SUFFIX = ".jpg";
    @Inject
    protected ViewModelProvider.Factory mFactory;
    @Inject @ActivityContext
    protected Context mContext;
    private FragmentAddBinding mBinding;
    private MainViewModel mViewModel;

    public AddFragment() {
    }

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
        mBinding.setVariable(BR.addphotohandler, (AddPhotoHandler) view -> {
            takePhoto();
        });
        mBinding.setVariable(BR.savesouvenirhandler, (SaveSouvenirHandler) view -> {
            String story = mBinding.etStory.getText().toString();
            String title = mBinding.etSouvenirTitle.getText().toString();
            String place = "hi";
            SouvenirSaveInfo info = new SouvenirSaveInfo(story, title, place);
            if (info.hasMissingValues()) {
                Toast.makeText(mContext, "There are missing values. Please input them", Toast.LENGTH_SHORT).show();
            } else {
                mViewModel.addSouvenir(info);
            }
        });
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ((intent.resolveActivity(mContext.getPackageManager())) != null) {
            File output = null;
            try {
                output = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (output != null) {
                Uri photoUri = FileProvider.getUriForFile(mContext,
                        "me.worric.souvenarius.fileprovider",
                        output);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            } else {
                Toast.makeText(mContext, "Could allocate temporary file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        String imageFileName = FILE_NAME_PREFIX + timestamp + FILE_NAME_SEPARATOR;
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, FILE_NAME_SUFFIX, storageDir);
        mViewModel.setPhotoPath(image);
        return image;
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
            if (photo != null) {
                souvenir.addImage(photo.getName());
            }
            return souvenir;
        }
    }

}
