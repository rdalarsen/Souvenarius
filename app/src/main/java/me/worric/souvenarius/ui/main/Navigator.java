package me.worric.souvenarius.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import me.worric.souvenarius.ui.authwrapper.AppUser;

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

    void initNavigation(@Nullable Bundle savedInstanceState, @Nullable AppUser user,
                        @Nullable Intent launchIntent);

}
