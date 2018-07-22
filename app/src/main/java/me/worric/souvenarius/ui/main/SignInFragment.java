package me.worric.souvenarius.ui.main;

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
import timber.log.Timber;


public class SignInFragment extends Fragment {

    private FragmentSigninBinding mBinding;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_CONNECTIVITY_CHANGED);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra(MainActivity.KEY_IS_CONNECTED, false);
            Timber.i("received internet connection status - setting in UI...");
            mBinding.setHasInternet(isConnected);
        }
    };

    private SignInButtonClickedListener mListener = hasInternetAccess -> {
        ((MainActivity)getActivity()).handleSignInButtonClicked(hasInternetAccess);
    };

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
