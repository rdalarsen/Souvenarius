package me.worric.souvenarius.ui.signin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class CredentialVerifierTest {

    /* RegEx taken directly from Google's implementation */
    private final Pattern mEmailPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );
    private CredentialVerifier mCredentialVerifier;

    @Before
    public void setUp() {
        mCredentialVerifier = new CredentialVerifier(mEmailPattern);
    }

    @Test(expected = NullPointerException.class)
    public void givenNullInput_checkIfValidEmail_shouldThrowNpe() {
        mCredentialVerifier.checkIfValidEmail(null);
    }

    @Test
    public void givenInvalidEmailAddressContainingSpace_checkIsValidEmail_shouldReturnFalse() {
        final String invalidEmailContainingSpace = "hej do@gmail.com";

        final boolean result = mCredentialVerifier.checkIfValidEmail(invalidEmailContainingSpace);

        assertThat(result, is(false));
    }

    @Test
    public void givenInvalidEmailAddressContainingSpecialCharacters_checkIsValidEmail_shouldReturnFalse() {
        final String invalidEmailContainingSpecialCharacters = "#lars!hans¤n@gmail.com";

        final boolean result = mCredentialVerifier
                .checkIfValidEmail(invalidEmailContainingSpecialCharacters);

        assertThat(result, is(false));
    }

    @Test
    public void givenEmptyStringAsInvalidEmailAddress_checkIsValidEmail_shouldReturnFalse() {
        final String invalidEmailContainingSpecialCharacters = "";

        final boolean result = mCredentialVerifier
                .checkIfValidEmail(invalidEmailContainingSpecialCharacters);

        assertThat(result, is(false));
    }

    @Test
    public void givenValidEmailAddress_checkIsValidEmail_shouldReturnTrue() {
        final String invalidEmailContainingSpecialCharacters = "larshansen@gmail.com";

        final boolean result = mCredentialVerifier
                .checkIfValidEmail(invalidEmailContainingSpecialCharacters);

        assertThat(result, is(true));
    }

}