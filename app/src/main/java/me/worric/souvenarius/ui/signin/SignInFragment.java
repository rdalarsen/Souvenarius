package me.worric.souvenarius.ui.signin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentSigninBinding;
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.main.MainActivity;
import timber.log.Timber;


public class SignInFragment extends Fragment {

    private static final String KEY_IS_CONNECTED = "key_is_connected";
    private FragmentSigninBinding mBinding;
    private boolean mIsConnected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_signin, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mListener);
        mIsConnected = initIsConnected(savedInstanceState);
        mBinding.setIsConnected(mIsConnected);
    }

    private boolean initIsConnected(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return NetUtils.getIsConnected(getContext());
        } else if (savedInstanceState.containsKey(MainActivity.KEY_IS_CONNECTED)) {
            return savedInstanceState.getBoolean(KEY_IS_CONNECTED, false);
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_CONNECTIVITY_CHANGED);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mReceiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_CONNECTED, mIsConnected);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra(MainActivity.KEY_IS_CONNECTED, false);
            Timber.i("Received connection broadcast. we are connected=%s", isConnected);
            boolean needsUpdate = !(mIsConnected == isConnected);
            if (needsUpdate) {
                mIsConnected = isConnected;
                Timber.i("connection status changed, setting new value...");
                mBinding.setIsConnected(mIsConnected);
            }
        }
    };

    private SignInButtonClickedListener mListener = hasInternetAccess ->
            ((MainActivity)getActivity()).handleSignInButtonClicked(hasInternetAccess);

    public interface SignInButtonClickedListener {
        void onSignInButtonClicked(boolean hasInternetAccess);
    }

    public static SignInFragment newInstance() {

        Bundle args = new Bundle();

        SignInFragment fragment = new SignInFragment();
        fragment.setArguments(args);
        return fragment;
    }

}