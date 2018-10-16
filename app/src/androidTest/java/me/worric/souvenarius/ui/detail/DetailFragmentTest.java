package me.worric.souvenarius.ui.detail;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.db.AppDatabase;
import me.worric.souvenarius.ui.main.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class DetailFragmentTest {

    @Rule
    public final IntentsTestRule<MainActivity> mRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void detailFragment_onShareSouvenirClick_correctIntentIsLaunched() {
        final String shareDialogTitle = InstrumentationRegistry.getTargetContext()
                .getString(R.string.share_dialog_title_detail);

        // Prevent launching the actual intent (which otherwise creates a non-dismissible chooser window)
        intending(anyIntent())
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.rv_souvenir_list))
                .perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.action_detail_share_souvenir))
                .perform(click());

        // Assert the correctness of the intent
        intended(allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(Intent.EXTRA_TITLE, shareDialogTitle),
                hasExtraWithKey(Intent.EXTRA_INTENT)
        ));
    }

    @Test
    public void detailFragment_navigateToDetailFragment_hidesFab() {
        onView(withId(R.id.rv_souvenir_list))
                .perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.btn_fab_add_souvenir))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void detailFragment_navigateToDetailFragment_showsCorrectSouvenirDetails() {
        onView(withId(R.id.rv_souvenir_list))
                .perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.tv_detail_title))
                .check(matches(withText(AppDatabase.MY_TITLE)));
        onView(withId(R.id.tv_detail_place))
                .check(matches(withText(AppDatabase.MY_PLACE)));
        onView(withId(R.id.tv_detail_story))
                .check(matches(withText(AppDatabase.MY_STORY)));
        onView(withId(R.id.tv_detail_num_photos))
                .check(matches(withText(AppDatabase.NUM_PHOTOS)));
        onView(withId(R.id.tv_detail_timestamp))
                .check(matches(withText(AppDatabase.FORMATTED_DATE)));
    }
    
}