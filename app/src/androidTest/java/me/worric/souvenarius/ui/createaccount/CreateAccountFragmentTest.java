package me.worric.souvenarius.ui.createaccount;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import me.worric.libtestrule.DisableAnimationsRule;
import me.worric.souvenarius.R;
import me.worric.souvenarius.ui.main.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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