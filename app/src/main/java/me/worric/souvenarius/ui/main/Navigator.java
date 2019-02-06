package me.worric.souvenarius.ui.main;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.Nullable;

public interface Navigator {

    int LAUNCHED_FROM_HISTORY_AND_IN_NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;

    void navigateToDetail(String souvenirId);

    void navigateToAdd();

    void navigateToSearch();

    void navigateToSignIn();

    void navigateToMain();

    void navigateBack();

    void navigateToCreateAccount();

    void initNavigation(@Nullable Bundle savedInstanceState, @Nullable FirebaseUser user,
                        @Nullable Intent launchIntent);

}
