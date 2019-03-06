package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;

public abstract class AbstractAppUser implements AppUser {

    private final FirebaseUser mFirebaseUser;

    public AbstractAppUser(FirebaseUser firebaseUser) {
        mFirebaseUser = firebaseUser;
    }

    @Override
    public String getUid() {
        return mFirebaseUser.getUid();
    }

    @Override
    public String getEmail() {
        return mFirebaseUser.getEmail();
    }

    @Override
    public String getDisplayName() {
        return mFirebaseUser.getDisplayName();
    }

    @Override
    public int hashCode() {
        return mFirebaseUser.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return mFirebaseUser.equals(obj);
    }

}
