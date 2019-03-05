package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class AbstractAppAuth implements AppAuth {

    private final FirebaseAuth mFirebaseAuth;
    private AppUser mAppUser;

    public AbstractAppAuth() {
        this(FirebaseAuth.getInstance());
    }

    public AbstractAppAuth(FirebaseAuth firebaseAuth) {
        mFirebaseAuth = firebaseAuth;
    }

    @Nullable
    @Override
    public AppUser getCurrentUser() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
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
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                result.onSuccess();
            } else {
                result.onFailure();
            }
        });
    }

    @Nullable
    @Override
    public String getUid() {
        return mFirebaseAuth.getUid();
    }

}
