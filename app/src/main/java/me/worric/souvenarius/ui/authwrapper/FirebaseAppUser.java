package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;

public class FirebaseAppUser implements AppUser {

    private final FirebaseUser mFirebaseUser;

    public FirebaseAppUser(FirebaseUser firebaseUser) {
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
        if (obj == null || obj instanceof FirebaseAppUser || obj instanceof FirebaseUser) {
            return mFirebaseUser.equals(obj);
        }

        throw new IllegalArgumentException("Cannot check for equality - object is not an instance " +
                "of either " + FirebaseAppUser.class.getSimpleName() + " or " +
                FirebaseUser.class.getSimpleName() + ". Instead was: " + obj.toString());
    }

}
