package me.worric.souvenarius.ui.main;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.worric.libtestrule.DisableAnimationsRule;
import me.worric.souvenarius.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
        onView(withId(R.id.fab_main_addSouvenir))
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
    public void onHeaderSearchButtonClick_searchFragmentIsDisplayed() {
        onView(withId(R.id.btn_main_headerSearchIcon))
                .perform(click());

        onView(withId(com.google.android.material.R.id.search_src_text))
                .check(matches(isDisplayed()));
        onView(withId(R.id.tb_search_searchToolBar))
                .check(matches(isDisplayed()));
    }

}