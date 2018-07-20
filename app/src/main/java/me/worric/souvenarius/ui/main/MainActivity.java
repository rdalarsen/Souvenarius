package me.worric.souvenarius.ui.main;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.databinding.ActivityMainBinding;
import me.worric.souvenarius.ui.add.AddFragment;
import me.worric.souvenarius.ui.detail.DetailFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    public static final int REQUEST_CODE = 404;
    private static final String KEY_SHOW_FAB_STATUS = "key_show_fab_status";
    @Inject
    protected ViewModelProvider.Factory mFactory;
    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;
    @Inject
    DispatchingAndroidInjector<Fragment> mInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mMainViewModel = ViewModelProviders.of(this, mFactory).get(MainViewModel.class);
        mBinding.setViewmodel(mMainViewModel);
        mBinding.setLifecycleOwner(this);

        setSupportActionBar(mBinding.toolbar);

        checkPermissions();
        initFragment(savedInstanceState);
        restoreSavedValues(savedInstanceState);
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE);

        }
    }

    private void restoreSavedValues(Bundle savedInstanceState) {
        boolean shouldShowFab = true;
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SHOW_FAB_STATUS)) {
            shouldShowFab = savedInstanceState.getBoolean(KEY_SHOW_FAB_STATUS);
        }
        mBinding.setShouldShowFab(shouldShowFab);
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, MainFragment.newInstance(), "main")
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            Timber.i("grantResults length: %d.", grantResults.length);
            Timber.i("Permission: %s was %s. Permission %s was %s",
                    permissions[0],
                    grantResults[0] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "REJECTED",
                    permissions[1],
                    grantResults[1] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "REJECTED");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Timber.i("success called");
            } else {
                Timber.i("failed called");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SHOW_FAB_STATUS, mBinding.getShouldShowFab());
    }

    public void handleItemClicked(SouvenirDb souvenir) {
        long souvenirId = souvenir.getId();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, DetailFragment.newInstance(souvenirId), "detail")
                .addToBackStack(null)
                .commit();
    }

    public void handleFab(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AddFragment.newInstance(), "add")
                .addToBackStack(null)
                .commit();
        mBinding.setShouldShowFab(false);
        mBinding.appbarLayout.setExpanded(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBinding.appbarLayout.setExpanded(true);
    }

    public void onSouvenirDeleted() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return mInjector;
    }
}
