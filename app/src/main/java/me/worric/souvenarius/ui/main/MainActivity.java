package me.worric.souvenarius.ui.main;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.databinding.ActivityMainBinding;
import me.worric.souvenarius.ui.add.AddFragment;
import me.worric.souvenarius.ui.detail.DetailFragment;
import me.worric.souvenarius.ui.signin.SignInFragment;
import me.worric.souvenarius.ui.widget.SouvenirWidgetProvider;
import me.worric.souvenarius.ui.widget.UpdateWidgetService;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    public static final String KEY_IS_CONNECTED = "key_is_connected";
    public static final String ACTION_CONNECTIVITY_CHANGED = "action_connectivity_changed";
    public static final String ACTION_AUTH_SIGNED_IN = "action_auth_signed_in";
    public static final String ACTION_AUTH_SIGNED_OUT = "action_auth_signed_out";
    private static final int RC_PERMISSION_RESULTS = 404;
    private static final int RC_SIGN_IN_ACTIVITY = 909;
    private Boolean mIsConnected;
    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;
    private FirebaseAuth mAuth;
    private LocalBroadcastManager mBroadcastManager;
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
            }, RC_PERMISSION_RESULTS);
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
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_IS_CONNECTED)) {
            mIsConnected = savedInstanceState.getBoolean(KEY_IS_CONNECTED);
        }
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return;
        } else if (mAuth.getCurrentUser() == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, SignInFragment.newInstance())
                    .commit();
            return;
        }

        Intent launchIntent = getIntent();
        if (launchIntent != null) {
            String action = launchIntent.getAction();
            if (TextUtils.isEmpty(action)) throw new IllegalArgumentException("Action cannot be null or empty");
            Timber.i("action of launch intent is: %s", action);

            switch (action) {
                case SouvenirWidgetProvider.ACTION_WIDGET_LAUNCH_ADD_SOUVENIR:
                    // handle widget action of launcing add souvenir
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, MainFragment.newInstance())
                            .setReorderingAllowed(true)
                            .commit();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, AddFragment.newInstance())
                            .setReorderingAllowed(true)
                            .addToBackStack(null)
                            .commit();
                    break;
                case SouvenirWidgetProvider.ACTION_WIDGET_LAUNCH_SOUVENIR_DETAILS:
                    // handle widget action of launching souvenir details
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, MainFragment.newInstance())
                            .setReorderingAllowed(true)
                            .commit();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, DetailFragment.newInstance(getIntent()
                                    .getStringExtra(SouvenirWidgetProvider.EXTRA_SOUVENIR_ID)))
                            .setReorderingAllowed(true)
                            .addToBackStack(null)
                            .commit();
                    break;
                case Intent.ACTION_MAIN:
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, MainFragment.newInstance())
                            .commit();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action: " + action);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            // alert other components of the app that they can / cannot use signed-in services
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
                Timber.i("login result ok - should add new fragment\nwe should also update appwidgets to reflect this.");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, MainFragment.newInstance(), "main")
                        .commit();
                UpdateWidgetService.startWidgetUpdate(this);
                mBroadcastManager.sendBroadcast(new Intent(ACTION_AUTH_SIGNED_IN));
            } else {
                Timber.w("login unsuccessful - should keep login fragment");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_CONNECTED, mIsConnected);
    }

    public void handleItemClicked(SouvenirDb souvenir) {
        String souvenirId = souvenir.getId();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, DetailFragment.newInstance(souvenirId), "detail")
                .addToBackStack(null)
                .commit();
    }

    public void onAddFabClicked(View view) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AddFragment.newInstance(), "add")
                .addToBackStack(null)
                .commit();
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


    private BroadcastReceiver mConnectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) return;
            NetworkInfo info = manager.getActiveNetworkInfo();
            boolean isConnected = info != null && info.isConnected();
            if (mIsConnected == null) {
                mIsConnected = isConnected;
                Timber.i("new connected status triggered (from null!)");
                sendConnectivityBroadcast();
            } else {
                boolean needsUpdate = !(mIsConnected == isConnected);
                if (needsUpdate) {
                    mIsConnected = isConnected;
                    Timber.i("new connected status triggered");
                    sendConnectivityBroadcast();
                }
            }
        }
    };

    private void sendConnectivityBroadcast() {
        Intent intent = new Intent(ACTION_CONNECTIVITY_CHANGED);
        intent.putExtra(KEY_IS_CONNECTED, mIsConnected);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void handleSignInButtonClicked(boolean isConnected) {
        if (isConnected) {
            launchSignInActivity();
        } else {
            Toast.makeText(this, R.string.main_connectivity_error_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void handleFabState(FabState fabState) {
        mBinding.setFabState(fabState);
    }

    public void handleSignOut() {
        mAuth.signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SignInFragment.newInstance())
                .commit();
        UpdateWidgetService.startWidgetUpdate(this);
        mBroadcastManager.sendBroadcast(new Intent(ACTION_AUTH_SIGNED_OUT));
    }

}
