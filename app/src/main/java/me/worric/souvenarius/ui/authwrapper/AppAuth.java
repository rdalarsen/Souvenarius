package me.worric.souvenarius.ui.authwrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface AppAuth {

    interface AppAuthResult {
        void onSuccess(@NonNull AppUser user);
        void onFailure(@NonNull Exception e);
    }

    @Nullable
    AppUser getCurrentUser();

    void signInWithEmailAndPassword(@NonNull String email,
                                    @NonNull String password,
                                    @NonNull AppAuthResult result);

    @Nullable
    String getUid();

    void signOut();

}
