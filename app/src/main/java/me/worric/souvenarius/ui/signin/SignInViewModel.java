package me.worric.souvenarius.ui.signin;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;

import timber.log.Timber;

public class SignInViewModel extends ViewModel {

    private CredentialVerifier mVerifier;
    private boolean isErrorModeEnabled;
    private MutableLiveData<String> mEmailContent;
    private MutableLiveData<SignInError> mEmailError;
    private LiveData<Boolean> mHasError;

    public SignInViewModel() {
        mVerifier = new CredentialVerifier(Patterns.EMAIL_ADDRESS);
        isErrorModeEnabled = false;
        mEmailContent = new MutableLiveData<>();
        mEmailError = new MutableLiveData<>();
        /*mEmailError = Transformations.map(mEmailContent, content -> {
            if (!isErrorModeEnabled) return null;

            if (TextUtils.isEmpty(content)) {
                return SignInError.EMAIL_NO_INPUT;
            }
            if (!mVerifier.checkIfValidEmail(content)) {
                return SignInError.EMAIL_INVALID;
            }
            return null;
        });*/
        mHasError = Transformations.map(mEmailContent, content -> {
            boolean emptyText = TextUtils.isEmpty(content);
            boolean invalidEmail = !mVerifier.checkIfValidEmail(content);
            if (isErrorModeEnabled) {
                if (emptyText) {
                    // set empty text
                    mEmailError.setValue(SignInError.EMAIL_NO_INPUT);
                } else if (invalidEmail) {
                    // set invalid email
                    mEmailError.setValue(SignInError.EMAIL_INVALID);
                }
            }
            boolean result = isErrorModeEnabled && (emptyText || invalidEmail);
            Timber.d("hasEmailError-result=%s", result);
            return result;
        });
    }

    public MutableLiveData<String> getEmailContent() {
        return mEmailContent;
    }

    public LiveData<SignInError> getEmailError() {
        return mEmailError;
    }

    public void performSignIn() {
        isErrorModeEnabled = true;
        mEmailContent.setValue(mEmailContent.getValue());
    }

    public LiveData<Boolean> getHasEmailError() {
        Timber.d("getHasEmailError triggered!");
        return mHasError;
    }

    private static class NullInitializedMutableLiveData<T> extends MutableLiveData<T> {
        public NullInitializedMutableLiveData() {
            super();
            setValue(null);
        }
    }

    private static class DelayedNullPostingMutableLiveData<T> extends MutableLiveData<T> {
        private Handler mHandler = new Handler(Looper.getMainLooper());
        private Runnable mDelayedNull = () -> {
            Timber.w("Executing null value setting.");
            super.setValue(null);
        };

        @Override
        public void setValue(T value) {
            Timber.i("Setting value. Scheduling posting of null value...");
            super.setValue(value);
            mHandler.postDelayed(mDelayedNull, 2000L);
        }

        @Override
        protected void onInactive() {
            Timber.d("onInactive called");
            mHandler.removeCallbacks(mDelayedNull);
            super.onInactive();
        }
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

}
