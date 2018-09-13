package me.worric.souvenarius.ui.main;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.worric.souvenarius.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> mTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void onStartup_SouvenirsListIsDisplayed() {
        onView(withId(R.id.rv_souvenir_list))
                .check(matches(isDisplayed()));
    }

    @Test
    public void onFabClick_addFragmentIsDisplayed() {
        onView(withId(R.id.btn_fab_add_souvenir))
                .perform(click());

        onView(withId(R.id.cv_add_container))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btn_add_save_souvenir))
                .check(matches(isDisplayed()));
    }

}