package me.worric.souvenarius.ui.main;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.worric.souvenarius.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private static final String DUMMY_TEXT = "This is the message";

    @Rule
    public final ActivityTestRule<MainActivity> mTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void onStartup_DummyTextIsDisplayed() {
        onView(withId(R.id.tv_hello_world)).check(matches(isDisplayed()));
    }

    @Test
    public void onStartup_DummyTextHasCorrectValue() {
        onView(withId(R.id.tv_hello_world)).check(matches(withText(DUMMY_TEXT)));
    }

}