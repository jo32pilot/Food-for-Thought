/**
 * File containing all the unit test for the application.
 *
 * @author Trevor Thomas
 */
package com.example.foodforthought;

import android.content.Context;

import androidx.fragment.app.testing.FragmentScenario;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import androidx.test.espresso.matcher.RootMatchers;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.foodforthought.Controller.LoginFragment;
import com.example.foodforthought.Controller.MainActivity;
import com.example.foodforthought.Controller.RecipeFragment;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    /**
     * Before any tests can be ran, the user must be logged out.
     */
    @Before
    public void logTheUserOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void testLoginFragment() {
        FragmentScenario.launchInContainer(LoginFragment.class);

        // check that this it the login page
        onView(withId(R.id.loginButton)).check(matches(withText("Login")));

        // enter the username and password
        onView(withId(R.id.emailLogin)).perform(typeText("tathomas@ucsd.edu"));
        onView(withId(R.id.passwordLogin)).perform(typeText("password"));

        // press the login button
        onView(withId(R.id.loginButton)).perform(scrollTo(), click());
    }

    @Test
    public void testRecipeFragment() {
        FragmentScenario.launchInContainer(RecipeFragment.class);

    }

    @Test
    public void testInventoryFragment() {

    }

    @Test
    public void testShoppingFragment() {

    }

    @Test
    public void testSignUpFragment() {

    }

    @Test
    public void testProfileFragment() {

    }

    @Test
    public void testMainFeedFragment() {

    }

    @Test
    public void testSavedRecipesFragment() {

    }

    @Test
    public void testAddRecipeFragment() {

    }
}