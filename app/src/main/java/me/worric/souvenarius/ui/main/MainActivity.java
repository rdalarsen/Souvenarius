package me.worric.souvenarius.ui.main;

import android.Manifest;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.UpdateSouvenirsService;
import me.worric.souvenarius.databinding.ActivityMainBinding;
import me.worric.souvenarius.ui.add.AddFragment;
import me.worric.souvenarius.ui.common.FabStateChanger;
import me.worric.souvenarius.ui.detail.DetailFragment;
import me.worric.souvenarius.ui.search.SearchFragment;
import me.worric.souvenarius.ui.signin.SignInFragment;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector,
        FabStateChanger, DetailFragment.DetailFragmentEventListener,
        MainFragment.MainFragmentEventListener, AddFragment.AddFragmentEventListener,
        SearchFragment.SearchFragmentEventListener, SignInFragment.SignInFragmentEventListener {

    public static final String KEY_IS_CONNECTED = "key_is_connected";
    public static final String ACTION_CONNECTIVITY_CHANGED = "action_connectivity_changed";
    private static final int RC_PERMISSION_RESULTS = 404;
    private static final int RC_SIGN_IN_ACTIVITY = 909;
    private Boolean mIsConnected;
    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;
    private FirebaseAuth mAuth;
    private LocalBroadcastManager mBroadcastManager;
    @Inject
    protected Navigator mNavigator;
    @Inject
    protected ViewModelProvider.Factory mFactory;
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
        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        checkLocationPermissions();
        mNavigator.initNavigation(savedInstanceState, mAuth.getCurrentUser(), getIntent());
        restoreSavedValues(savedInstanceState);
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, RC_PERMISSION_RESULTS);
        }
    }

    private void restoreSavedValues(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_IS_CONNECTED)) {
            mIsConnected = savedInstanceState.getBoolean(KEY_IS_CONNECTED);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionStateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mConnectionStateReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mIsConnected != null) {
            outState.putBoolean(KEY_IS_CONNECTED, mIsConnected);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBinding.appbarLayout.setExpanded(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION_RESULTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Timber.i("All permissions granted");
            } else {
                Timber.i("Permissions not granted");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                mNavigator.navigateToMain();
                mMainViewModel.updateUserId(mAuth.getUid());
                UpdateSouvenirsService.startSouvenirsUpdate(this);
            } else {
                Timber.w("login unsuccessful");
            }
        }
    }

    /*
    * MainActivity callbacks
    */
    public void handleAddFabClicked(View view) {
        mNavigator.navigateToAdd();
        mBinding.appbarLayout.setExpanded(true);
    }

    public void handleShowConnectionWarningToast(View view) {
        Toast.makeText(this, R.string.error_message_offline_mode, Toast.LENGTH_SHORT).show();
    }

    /*
    * DetailFragment callbacks
    */
    @Override
    public void onSouvenirDeleted() {
        mNavigator.navigateBack();
    }

    /*
    * AddFragment callbacks
    */
    @Override
    public void onSouvenirSaved() {
        mNavigator.navigateBack();
    }

    /*
    * SignInFragment callbacks
    */
    @Override
    public void onSignInClicked(boolean isConnected) {
        if (isConnected) {
            launchSignInActivity();
        } else {
            Toast.makeText(this, R.string.main_connectivity_error_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchSignInActivity() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .build(), RC_SIGN_IN_ACTIVITY);
    }

    /*
    * MainFragment callbacks
    */
    @Override
    public void onSouvenirClicked(SouvenirDb souvenir) {
        mNavigator.navigateToDetail(souvenir.getId());
        mBinding.appbarLayout.setExpanded(true);
    }

    @Override
    public void onSignOutClicked() {
        mAuth.signOut();
        mNavigator.navigateToSignIn();
        mMainViewModel.updateUserId(null);
        UpdateWidgetService.startWidgetUpdate(this);
    }

    @Override
    public void onSearchClicked() {
        mNavigator.navigateToSearch();
        mBinding.appbarLayout.setExpanded(true);
    }

    /*
    * SearchFragment callbacks
    */
    @Override
    public void onClearButtonClicked() {
        mNavigator.navigateBack();
        mBinding.appbarLayout.setExpanded(true);
    }

    @Override
    public void onSearchResultClicked(SouvenirDb souvenir) {
        mNavigator.navigateToDetail(souvenir.getId());
        mBinding.appbarLayout.setExpanded(true);
    }

    /*
    * FabStateChanger callback
    */
    @Override
    public void changeFabState(FabState fabState) {
        mBinding.setFabState(fabState);
    }

    private BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) return;
            NetworkInfo info = manager.getActiveNetworkInfo();
            boolean isConnected = info != null && info.isConnected();
            mBinding.setIsConnected(isConnected);
            if (mIsConnected == null) {
                mIsConnected = isConnected;
                sendLocalConnectivityBroadcast();
            } else {
                boolean needsUpdate = !(mIsConnected == isConnected);
                if (needsUpdate) {
                    mIsConnected = isConnected;
                    sendLocalConnectivityBroadcast();
                }
            }
        }
    };

    private void sendLocalConnectivityBroadcast() {
        Intent intent = new Intent(ACTION_CONNECTIVITY_CHANGED);
        intent.putExtra(KEY_IS_CONNECTED, mIsConnected);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return mInjector;
    }
}
