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
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentSigninBinding;
import me.worric.souvenarius.ui.common.FabStateChanger;
import me.worric.souvenarius.ui.common.NetUtils;
import me.worric.souvenarius.ui.main.FabState;
import me.worric.souvenarius.ui.main.MainActivity;


public class SignInFragment extends Fragment {

    private static final String KEY_IS_CONNECTED = "key_is_connected";
    private FragmentSigninBinding mBinding;
    private boolean mIsConnected;
    private FabStateChanger mFabStateChanger;
    private SignInFragmentEventListener mSignInFragmentEventListener;
    private CredentialVerifier mCredentialVerifier;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mFabStateChanger = (FabStateChanger) context;
            mSignInFragmentEventListener = (SignInFragmentEventListener) context;
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Attached activity does not implement either" +
                    " FabStateChanger or SignInFragmentEventListener or both: " + context.toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCredentialVerifier = new CredentialVerifier(Patterns.EMAIL_ADDRESS);
        mIsConnected = initIsConnected(savedInstanceState);
    }

    private boolean initIsConnected(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return NetUtils.isConnected(getContext());
        } else if (savedInstanceState.containsKey(MainActivity.KEY_IS_CONNECTED)) {
            return savedInstanceState.getBoolean(KEY_IS_CONNECTED, false);
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_signin, container, false);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mListener);
        mBinding.setIsConnected(mIsConnected);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_CONNECTIVITY_CHANGED);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mReceiver, filter);
        mFabStateChanger.changeFabState(FabState.HIDDEN);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mFabStateChanger = null;
        mSignInFragmentEventListener = null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra(MainActivity.KEY_IS_CONNECTED, false);
            boolean needsUpdate = !(mIsConnected == isConnected);
            if (needsUpdate) {
                mIsConnected = isConnected;
                mBinding.setIsConnected(mIsConnected);
            }
        }
    };

    private SignInButtonClickListener mListener = isConnected -> {
        final String email = mBinding.etSigninEmail.getText().toString();
        final String password = mBinding.etSigninPassword.getText().toString();

        if (mCredentialVerifier.checkIfValidEmail(email)) {
            Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
//            mSignInFragmentEventListener.onSignInClicked(isConnected);
        } else {
            mBinding.tilSigninEmail.setError(getString(R.string.error_message_sign_in_invalid_email));
            requestFocusOnError(mBinding.etSigninEmail);
        }
    };

    private void requestFocusOnError(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mBinding.etSigninEmail, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public interface SignInButtonClickListener {
        void onSignInButtonClicked(boolean isConnected);
    }

    public interface SignInFragmentEventListener {
        void onSignInClicked(boolean isConnected);
    }

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    public SignInFragment() {
    }

}
