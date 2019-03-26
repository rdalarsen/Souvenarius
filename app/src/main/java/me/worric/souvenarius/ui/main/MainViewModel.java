package me.worric.souvenarius.ui.main;

import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;
import me.worric.souvenarius.ui.signin.CredentialVerifier;
import me.worric.souvenarius.ui.signin.SignInFragment;
import timber.log.Timber;

public class MainViewModel extends ViewModel implements SignInFragment.SignInFeature {

    private final SouvenirRepository mSouvenirRepository;
    private final LiveData<Integer> mNumSouvenirs;
    private final LiveData<Result<List<SouvenirDb>>> mSouvenirs;
    private final MutableLiveData<Boolean> mIsConnected;
    private final MutableLiveData<FabState> mFabState;

    private final CredentialVerifier mCredentialVerifier;
    private boolean isErrorModeEnabled;
    private MutableLiveData<String> mEmailContent;
    private MutableLiveData<SignInFragment.SignInError> mEmailError;
    private LiveData<Boolean> mHasError;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository, CredentialVerifier credentialVerifier) {
        mSouvenirRepository = souvenirRepository;
        mCredentialVerifier = credentialVerifier;
        mIsConnected = new MutableLiveData<>();
        mFabState = new MutableLiveData<>();
        mSouvenirs = mSouvenirRepository.getSouvenirs();
        mNumSouvenirs = Transformations.map(mSouvenirs, result -> {
            if (result != null && result.status.equals(Result.Status.SUCCESS)) {
                return result.response.size();
            }
            return null;
        });

        isErrorModeEnabled = false;
        mEmailContent = new MutableLiveData<>();
        mEmailError = new MutableLiveData<>();
        mHasError = Transformations.map(mEmailContent, content -> {
            boolean emptyText = TextUtils.isEmpty(content);
            boolean invalidEmail = !mCredentialVerifier.checkIfValidEmail(content);
            if (isErrorModeEnabled) {
                if (emptyText) {
                    // set empty text
                    mEmailError.setValue(SignInFragment.SignInError.EMAIL_NO_INPUT);
                } else if (invalidEmail) {
                    // set invalid email
                    mEmailError.setValue(SignInFragment.SignInError.EMAIL_INVALID);
                }
            }
            boolean result = isErrorModeEnabled && (emptyText || invalidEmail);
            Timber.d("hasEmailError-result=%s", result);
            return result;
        });
    }

    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
        return mSouvenirs;
    }

    public LiveData<Integer> getNumSouvenirs() {
        return mNumSouvenirs;
    }

    public void updateUserId(@Nullable String uid) {
        mSouvenirRepository.setQueryParameters(uid);
    }

    public void updateSortStyle(@NonNull SortStyle sortStyle) {
        mSouvenirRepository.setQueryParameters(sortStyle);
    }

    public LiveData<Boolean> getIsConnected() {
        return mIsConnected;
    }

    public void updateConnectedStatus(boolean isConnected) {
        boolean shouldUpdate = mIsConnected.getValue() != Boolean.valueOf(isConnected);
        Timber.d("updateConnectedStatus called with value=%s; will %s update status",
                isConnected, shouldUpdate ? "INDEED" : "NOT");
        if (shouldUpdate) mIsConnected.setValue(isConnected);
    }

    /**
     * Here we're purposefully exposing a MutableLiveData in order to support 2-way DataBinding
     *
     * @return LiveData holding email content
     */
    @Override
    public MutableLiveData<String> getEmailContent() {
        return mEmailContent;
    }

    @Override
    public LiveData<SignInFragment.SignInError> getEmailError() {
        return mEmailError;
    }

    public LiveData<FabState> getFabState() {
        return mFabState;
    }

    public void updateFabState(@NonNull FabState fabState) {
        mFabState.setValue(fabState);
    }

    @Override
    public void performSignIn() {
        isErrorModeEnabled = true;
        mEmailContent.setValue(mEmailContent.getValue());
    }

    @Override
    public LiveData<Boolean> getHasEmailError() {
        Timber.d("getHasEmailError triggered!");
        return mHasError;
    }

}
