package me.worric.souvenarius.ui.detail;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import me.worric.libtestrule.DisableAnimationsRule;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.RoomMockUtils;
import me.worric.souvenarius.ui.main.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class DetailFragmentTest {

    @Rule public final IntentsTestRule<MainActivity> mRule = new IntentsTestRule<>(MainActivity.class);
    @Rule public final DisableAnimationsRule mAnimationsRule = new DisableAnimationsRule();

    @Test
    public void detailFragment_onShareSouvenirClick_correctIntentIsLaunched() {
        final String shareDialogTitle = ApplicationProvider.getApplicationContext()
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

        onView(withId(R.id.fab_main_addSouvenir))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void detailFragment_navigateToDetailFragment_showsCorrectSouvenirDetails() {
        onView(withId(R.id.rv_souvenir_list))
                .perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.tv_detail_title))
                .check(matches(withText(RoomMockUtils.MY_TITLE)));
        onView(withId(R.id.tv_detail_place))
                .check(matches(withText(RoomMockUtils.MY_PLACE)));
        onView(withId(R.id.tv_detail_story))
                .check(matches(withText(RoomMockUtils.MY_STORY)));
        onView(withId(R.id.tv_detail_num_photos))
                .check(matches(withText(RoomMockUtils.NUM_PHOTOS)));
        onView(withId(R.id.tv_detail_timestamp))
                .check(matches(withText(RoomMockUtils.FORMATTED_DATE)));
    }

}