package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.ActivityMainBinding;
import me.worric.souvenarius.ui.GlideApp;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO_REQUEST_CODE = 909;
    @Inject
    protected ViewModelProvider.Factory mFactory;

    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mMainViewModel = ViewModelProviders.of(this, mFactory).get(MainViewModel.class);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setViewmodel(mMainViewModel);
        mBinding.setLifecycleOwner(this);
    }

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ((intent.resolveActivity(getPackageManager())) != null) {
            File output = null;
            try {
                output = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (output != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "me.worric.souvenarius.fileprovider",
                        output);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mMainViewModel.setPhotoPath(image);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.i("Photo was taken OK");
            } else {
                Timber.w("Could not take photo.");
            }
        }
    }

    @BindingAdapter({"imageUri", "placeholder"})
    public static void loadImage(ImageView view, String uri, Drawable placeholder) {
        GlideApp.with(view.getContext())
                .load(uri)
                .placeholder(placeholder)
                .into(view);
    }

}
