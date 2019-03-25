package me.worric.souvenarius.ui.signin;

import java.util.regex.Pattern;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Reusable;

@Reusable
public class CredentialVerifier {

    private final Pattern mPattern;

    @Inject
    public CredentialVerifier(Pattern pattern) {
        mPattern = pattern;
    }

    public boolean checkIfValidEmail(@NonNull String email) {
        return mPattern.matcher(email).matches();
    }

}
