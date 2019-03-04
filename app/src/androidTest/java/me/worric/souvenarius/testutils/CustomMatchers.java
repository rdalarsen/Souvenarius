package me.worric.souvenarius.testutils;

import android.view.View;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CustomMatchers {

    public static Matcher<View> hasTextInputLayoutError(final String expectedErrorTest) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }

                final CharSequence actualString = ((TextInputLayout) view).getError();

                if (actualString == null) {
                    return false;
                }

                return expectedErrorTest.equals(actualString.toString());
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

}
