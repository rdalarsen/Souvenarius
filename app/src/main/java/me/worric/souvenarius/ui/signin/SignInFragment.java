package me.worric.souvenarius.ui.signin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentSigninBinding;
import me.worric.souvenarius.ui.authwrapper.AppAuth;
import me.worric.souvenarius.ui.authwrapper.AppUser;
import me.worric.souvenarius.ui.main.MainViewModel;
import timber.log.Timber;


public class SignInFragment extends Fragment {

    private FragmentSigninBinding mBinding;
    private SignInFragmentEventListener mSignInFragmentEventListener;
    private MainViewModel mViewModel;
    @Inject CredentialVerifier mCredentialVerifier;
    @Inject AppAuth mAppAuth;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            mSignInFragmentEventListener = (SignInFragmentEventListener) context;
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Attached activity does not implement" +
                    " SignInFragmentEventListener: " + context.toString());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel.class);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_signin, container, false);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mClickListener);
        return mBinding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSignInFragmentEventListener = null;
    }

    private ClickListener mClickListener = new ClickListener() {
        @Override
        public void onSignInButtonClicked(boolean isConnected, String email, String password) {
            mViewModel.performSignIn();

            // TODO implement 2-way databinding in SignInFragment
            final boolean emailIsMissing = email.isEmpty();
            final boolean passwordIsMissing = password.isEmpty();

            if (emailIsMissing || passwordIsMissing) {
                Timber.e("Email is missing: %s. Password is missing: %s", emailIsMissing, passwordIsMissing);
                if (emailIsMissing) {
                    mBinding.tilSigninEmail.setError(getString(R.string.error_message_sign_in_empty_email_error));
                } else {
                    clearEmailInputError();
                }

                if (passwordIsMissing) {
                    mBinding.tilSigninPassword.setError(getString(R.string.error_message_sign_in_empty_password_error));
                } else {
                    clearPasswordInputError();
                }
                return;
            } else {
                clearAllInputErrors();
            }

            if (mCredentialVerifier.checkIfValidEmail(email)) {
                mAppAuth.signInWithEmailAndPassword(email, password, new AppAuth.AppAuthResult() {
                    @Override
                    public void onSuccess(@NonNull AppUser user) {
                        Timber.i("Auth attempt successful! Username=%s,email=%s,UID=%s",
                                user.getDisplayName(),
                                user.getEmail(),
                                user.getUid());

                        mSignInFragmentEventListener.onSignInSuccessful(isConnected);
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.e(e, "Auth attempt failed");
                        Toast.makeText(getContext(), "Could not log in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                mBinding.tilSigninEmail.setError(getString(R.string.error_message_sign_in_invalid_email));
                requestFocusOnError(mBinding.etSigninEmail);
            }
        }

        @Override
        public void onCreateAccountButtonClicked() {
            mSignInFragmentEventListener.onCreateAccountClicked();
        }
    };

    private void clearEmailInputError() {
        Timber.d("Clearing email input error");
        mBinding.tilSigninEmail.setErrorEnabled(false);
    }

    private void clearPasswordInputError() {
        Timber.d("Clearing password input error");
        mBinding.tilSigninPassword.setErrorEnabled(false);
    }

    private void clearAllInputErrors() {
        Timber.d("Clearing ALL input errors");
        mBinding.tilSigninEmail.setErrorEnabled(false);
        mBinding.tilSigninPassword.setErrorEnabled(false);
    }

    private void requestFocusOnError(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mBinding.etSigninEmail, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public interface ClickListener {
        void onSignInButtonClicked(final boolean isConnected, final String email, final String password);
        void onCreateAccountButtonClicked();
    }

    public interface SignInFragmentEventListener {
        void onSignInSuccessful(boolean isConnected);
        void onCreateAccountClicked();
    }

    public interface SignInFeature {
        MutableLiveData<String> getEmailContent();
        LiveData<SignInError> getEmailError();
        void performSignIn();
        LiveData<Boolean> getHasEmailError();
    }

    public enum SignInError {
        EMAIL_NO_INPUT("Empty email"),
        EMAIL_INVALID("Invalid email");

        private final String errorText;

        SignInError(String errorText) {
            this.errorText = errorText;
        }

        public String getErrorText() {
            return errorText;
        }
    }

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    public SignInFragment() {}

}
