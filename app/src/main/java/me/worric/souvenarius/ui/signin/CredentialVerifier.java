package me.worric.souvenarius.ui.signin;

import java.util.Objects;
import java.util.regex.Pattern;

public class CredentialVerifier {

    private final Pattern mPattern;

    public CredentialVerifier(Pattern pattern) {
        mPattern = pattern;
    }

    public boolean checkIfValidEmail(String email) {
        Objects.requireNonNull(email);
        return mPattern.matcher(email).matches();
    }

}
