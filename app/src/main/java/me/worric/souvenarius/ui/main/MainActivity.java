package me.worric.souvenarius.ui.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.UpdateSouvenirsService;
import me.worric.souvenarius.databinding.ActivityMainBinding;
import me.worric.souvenarius.ui.add.AddFragment;
import me.worric.souvenarius.ui.authwrapper.AppAuth;
import me.worric.souvenarius.ui.detail.DetailFragment;
import me.worric.souvenarius.ui.search.SearchFragment;
import me.worric.souvenarius.ui.signin.SignInFragment;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector,
        DetailFragment.DetailFragmentEventListener, MainFragment.MainFragmentEventListener,
        AddFragment.AddFragmentEventListener, SearchFragment.SearchFragmentEventListener,
        SignInFragment.SignInFragmentEventListener {

    private static final int RC_PERMISSION_RESULTS = 404;
    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;
    private ConnectivityManager mConnectivityManager;
    @Inject AppAuth mAppAuth;
    @Inject Navigator mNavigator;
    @Inject ViewModelProvider.Factory mFactory;
    @Inject DispatchingAndroidInjector<Fragment> mInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mMainViewModel = ViewModelProviders.of(this, mFactory).get(MainViewModel.class);
        mBinding.setViewmodel(mMainViewModel);
        mBinding.setLifecycleOwner(this);

        checkLocationPermissions();

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FabTweaker(), false);
        mNavigator.initNavigation(savedInstanceState, mAppAuth.getCurrentUser(), getIntent());
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Helper class that knows about the Fragments hosted in MainFragment and is able to change the
     * FAB visibility state accordingly.
     *
     * It is attached to the SupportFragmentManager of the Activity, where it listens for resumed
     * fragments and takes appropriate action.
     */
    private class FabTweaker extends FragmentManager.FragmentLifecycleCallbacks {

        private final Map<Class<? extends Fragment>,FabState> myMap;

        private FabTweaker() {
            myMap = new HashMap<>();
            myMap.put(MainFragment.class,FabState.ADD);
            myMap.put(DetailFragment.class,FabState.HIDDEN);
            myMap.put(SignInFragment.class,FabState.HIDDEN);
            myMap.put(SearchFragment.class,FabState.HIDDEN);
            myMap.put(AddFragment.class,FabState.HIDDEN);
        }

        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            if (!myMap.containsKey(f.getClass())) {
                Timber.w("Fragment of class %s not setup to configure FAB; skipping", f.getClass().getSimpleName());
                return;
            }

            FabState state = myMap.get(f.getClass());
            if (state != null) {
                Timber.d("Fragment resumed is of type: %s. Applying FAB state: %s",
                        f.getClass().getSimpleName(), state.toString());
                mMainViewModel.updateFabState(state);
            }
        }
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

    /*
    * MainActivity callbacks
    */
    public void handleAddFabClicked(View view) {
        mNavigator.navigateToAdd();
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
    public void onSignInSuccessful(boolean isConnected) {
        if (isConnected) {
            mNavigator.navigateToMain();
            mMainViewModel.updateUserId(mAppAuth.getUid());
            UpdateSouvenirsService.startSouvenirsUpdate(this);
        } else {
            Toast.makeText(this, R.string.main_connectivity_error_message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateAccountClicked() {
        mNavigator.navigateToCreateAccount();
    }

    /*
    * MainFragment callbacks
    */
    @Override
    public void onSouvenirClicked(SouvenirDb souvenir) {
        mNavigator.navigateToDetail(souvenir.getId());
    }

    @Override
    public void onSignOutClicked() {
        mAppAuth.signOut();
        mNavigator.navigateToSignIn();
        mMainViewModel.updateUserId(null);
        UpdateWidgetService.startWidgetUpdate(this);
    }

    @Override
    public void onSearchClicked() {
        mNavigator.navigateToSearch();
    }

    /*
    * SearchFragment callbacks
    */
    @Override
    public void onClearButtonClicked() {
        mNavigator.navigateBack();
    }

    @Override
    public void onSearchResultClicked(SouvenirDb souvenir) {
        mNavigator.navigateToDetail(souvenir.getId());
    }

    private BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received connectivity broadcast in MainActivity");
            NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
            boolean isConnected = info != null && info.isConnected();
            mMainViewModel.updateConnectedStatus(isConnected);
        }
    };

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return mInjector;
    }

}
