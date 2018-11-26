package me.worric.souvenarius.ui.signin;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.worric.libtestrule.DisableAnimationsRule;
import me.worric.souvenarius.R;
import me.worric.souvenarius.ui.main.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static me.worric.souvenarius.testutils.CustomMatchers.hasTextInputLayoutError;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class SignInFragmentTest {

    public static final String EMAIL_ERROR_MESSAGE = InstrumentationRegistry.getTargetContext()
            .getString(R.string.error_message_sign_in_empty_email_error);
    public static final String PASSWORD_ERROR_MESSAGE = InstrumentationRegistry.getTargetContext()
            .getString(R.string.error_message_sign_in_empty_password_error);
    public static final String TEST_EMAIL = "test@email.com";
    public static final String TEST_PASSWORD = "testpassword";

    @Rule public final ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);
    @Rule public final DisableAnimationsRule mAnimationsRule = new DisableAnimationsRule();

    @Test
    public void givenEmptyEmail_tappingLoginButton_shouldShowEmailError() {
        onView(withId(R.id.btn_signin_signin))
                .perform(click());

        onView(withId(R.id.til_signin_email))
                .check(matches(hasTextInputLayoutError(EMAIL_ERROR_MESSAGE)));
    }

    @Test
    public void givenEmptyPassword_tappingLoginButton_shouldShowPasswordError() {
        onView(withId(R.id.btn_signin_signin))
                .perform(click());

        onView(withId(R.id.til_signin_password))
                .check(matches(hasTextInputLayoutError(PASSWORD_ERROR_MESSAGE)));
    }

    @Test
    public void givenEmptyPasswordAndEmptyEmail_tappingLoginButton_shouldShowEmailAndPasswordErrors() {
        onView(withId(R.id.btn_signin_signin))
                .perform(click());

        onView(withId(R.id.til_signin_email))
                .check(matches(hasTextInputLayoutError(EMAIL_ERROR_MESSAGE)));
        onView(withId(R.id.til_signin_password))
                .check(matches(hasTextInputLayoutError(PASSWORD_ERROR_MESSAGE)));
    }

    @Test
    public void givenNonEmptyEmailButEmptyPassword_tappingLoginButton_shouldShowPasswordErrorOnly() {
        onView(withId(R.id.et_signin_email))
                .perform(typeText(TEST_EMAIL))
                .perform(closeSoftKeyboard());

        onView(withId(R.id.btn_signin_signin))
                .perform(click());

        onView(withId(R.id.til_signin_email))
                .check(matches(not(hasTextInputLayoutError(EMAIL_ERROR_MESSAGE))));
        onView(withId(R.id.til_signin_password))
                .check(matches(hasTextInputLayoutError(PASSWORD_ERROR_MESSAGE)));
    }

    @Test
    public void givenNonEmptyPasswordButEmptyEmail_tappingLoginButton_shouldShowEmailErrorOnly() {
        onView(withId(R.id.et_signin_password))
                .perform(typeText(TEST_PASSWORD))
                .perform(closeSoftKeyboard());

        onView(withId(R.id.btn_signin_signin))
                .perform(click());

        onView(withId(R.id.til_signin_email))
                .check(matches(hasTextInputLayoutError(EMAIL_ERROR_MESSAGE)));
        onView(withId(R.id.til_signin_password))
                .check(matches(not(hasTextInputLayoutError(PASSWORD_ERROR_MESSAGE))));
    }

}