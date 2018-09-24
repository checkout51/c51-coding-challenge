package com.c51.sedwards.c51challenge;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.c51.sedwards.c51challenge.model.Offer;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class OfferListTest {

    @Rule
    public ActivityTestRule<OfferListActivity> mActivityRule =
            new ActivityTestRule<>(OfferListActivity.class);

    @Test
    public void sortFromUi() throws InterruptedException {
        //make sure we turn off cash back sorting
        onView(withId(R.id.sort_name)).perform(click());
        onView(withId(R.id.sort_cash_back)).perform(click());
        onView(withId(R.id.offer_list_view)).check(matches(isDisplayed()));
        onView(withId(R.id.offer_list_view)).perform(RecyclerViewActions.scrollToPosition(0));
        onView(first(withText("$3.00 Cash Back"))).check(matches(isDisplayed()));
    }

    private <T> Matcher<T> first(final Matcher<T> matcher) {
        //https://stackoverflow.com/questions/32387137/espresso-match-first-element-found-when-many-are-in-hierarchy
        return new BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Will return first matching item");
            }
        };
    }
}
