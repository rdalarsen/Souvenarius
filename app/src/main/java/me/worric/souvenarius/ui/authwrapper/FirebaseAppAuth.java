package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

@Singleton
public class FirebaseAppAuth implements AppAuth {

    private final FirebaseAuth mFirebaseAuth;
    private AppUser mAppUser;

    @Inject
    public FirebaseAppAuth(FirebaseAuth firebaseAuth) {
        mFirebaseAuth = firebaseAuth;
    }

    @Nullable
    @Override
    public AppUser getCurrentUser() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        Timber.i("Getting current user. Cached=%s", user != null && user.equals(mAppUser));
        if (user != null) {
            if (!user.equals(mAppUser)) {
                mAppUser = new FirebaseAppUser(user);
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
                    result.onSuccess(new FirebaseAppUser(authResult.getUser()));
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
