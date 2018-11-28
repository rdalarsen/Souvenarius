package me.worric.souvenarius.ui.createaccount;

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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CreateAccountFragmentTest {

    @Rule public final ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);
    @Rule public final DisableAnimationsRule mAnimationsRule = new DisableAnimationsRule();

    @Test
    public void tappingCreateAccountButton_shouldShowCreateAccountScreen() {
        onView(withId(R.id.btn_signin_create))
                .perform(click());

        onView(withId(R.id.btn_createaccount_create))
                .perform(click())
                .check(matches(isDisplayed()));
    }

}