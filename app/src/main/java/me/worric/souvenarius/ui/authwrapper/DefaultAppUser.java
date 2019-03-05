package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;

public class DefaultAppUser extends AbstractAppUser {

    public DefaultAppUser(FirebaseUser firebaseUser) {
        super(firebaseUser);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

}
