package me.worric.souvenarius.ui.main;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;

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
    private static final int RC_SIGN_IN_ACTIVITY = 909;
    @Inject
    protected ViewModelProvider.Factory mFactory;
    @Inject
    protected SharedPreferences mSharedPreferences;
    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;
    private FirebaseAuth mAuth;
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

        mAuth = FirebaseAuth.getInstance();

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

    private void launchSignInActivity() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .build(), RC_SIGN_IN_ACTIVITY);
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
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionStateReceiver, filter);
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);
        unregisterReceiver(mConnectionStateReceiver);
    }

    private void updateUi(FirebaseUser currentUser) {
        if (currentUser == null) {
            Timber.i("current user is NULL");
        } else {
            Timber.i("current user NOT NULL; username is: %s", currentUser.getDisplayName());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                updateUi(mAuth.getCurrentUser());
            } else {
                updateUi(null);
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

    private FirebaseAuth.AuthStateListener mAuthStateListener = firebaseAuth -> {
        if (firebaseAuth.getCurrentUser() != null) {
            Timber.i("AuthStateListener: user is NOT NULL; username is: %s", firebaseAuth.getCurrentUser().getDisplayName());
        } else {
            Timber.i("AuthStateListener: user is NULL; launcing sign-in activity");
            launchSignInActivity();
        }
    };

    private BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) return;
            NetworkInfo info = manager.getActiveNetworkInfo();
            boolean isConnected = info != null && info.isConnected();
            Timber.i("Connection status: %s", isConnected ? "CONNECTED" : "NOT CONNECTED");
        }
    };

}
