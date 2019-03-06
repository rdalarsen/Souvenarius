package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public abstract class AbstractAppAuth implements AppAuth {

    private final FirebaseAuth mFirebaseAuth;
    private AppUser mAppUser;

    public AbstractAppAuth(FirebaseAuth firebaseAuth) {
        mFirebaseAuth = firebaseAuth;
    }

    @Nullable
    @Override
    public AppUser getCurrentUser() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        Timber.i("Getting current user. Cached=%s", user != null && user.equals(mAppUser));
        if (user != null) {
            if (!user.equals(mAppUser)) {
                mAppUser = new DefaultAppUser(user);
            }
        } else {
            mAppUser = null;
        }

        return mAppUser;
    }

    @Override
    public void signInWithEmailAndPassword(@NonNull String email, @NonNull String password, @NonNull AppAuthResult result) {
        Timber.i("Signing in. email=%s,password=%s", email, password);
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Timber.i("Sign in task is successful!");
                    result.onSuccess(new DefaultAppUser(authResult.getUser()));
                })
                .addOnFailureListener(result::onFailure);
    }

    @Nullable
    @Override
    public String getUid() {
        Timber.i("Returning UID: %s", mFirebaseAuth.getUid());
        return mFirebaseAuth.getUid();
    }

    @Override
    public void signOut() {
        Timber.i("Signing out");
        mFirebaseAuth.signOut();
    }

}
