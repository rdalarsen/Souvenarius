package me.worric.souvenarius.ui.main;

import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.worric.libtestrule.DisableAnimationsRule;
import me.worric.souvenarius.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> mTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public final DisableAnimationsRule mAnimationsRule = new DisableAnimationsRule();

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

    @Test
    public void onListItemClick_detailsFragmentIsDisplayed() {
        onView(withId(R.id.rv_souvenir_list))
                .perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.sv_detail_root))
                .check(matches(isDisplayed()));
        onView(withId(R.id.rv_souvenir_photo_list))
                .check(matches(isDisplayed()));
    }

    @Test
    public void onToolbarSearchButtonClick_searchFragmentIsDisplayed() {
        onView(withId(R.id.action_main_search))
                .perform(click());

        onView(withId(android.support.design.R.id.search_src_text))
                .check(matches(isDisplayed()));
        onView(withId(R.id.rv_search_result_list))
                .check(matches(isDisplayed()));
    }

}