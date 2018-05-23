package com.unilol.comp4521.unilol

import android.graphics.Point
import android.os.RemoteException
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.UiDevice
import org.junit.Before
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matchers.not


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @Rule
    @JvmField
    val rule  = ActivityTestRule(LoginActivity::class.java)

    private val userEmail="budiryan@gmail.com"
    private val correctPassword = "123456"
    private val wrongPassword = "654321"

    @Before
    fun init() {
        // Wake up the device just for the sake of not failing the test,
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val coordinates = arrayOfNulls<Point>(1)
        coordinates[0] = Point(0, 1600) // Press the top of the screen so it doesn't not touch the keyboard
        try {
            if (!uiDevice.isScreenOn) {
                uiDevice.wakeUp()
                uiDevice.swipe(coordinates, 10)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    @Test
    fun useAppContext() {
        // Basic hello world testing
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.unilol.comp4521.unilol", appContext.packageName)
    }

    @Test
    fun ensureLoginUsingEmailFail(){
        onView(withId(R.id.login_email))
                .perform(typeText(userEmail), closeSoftKeyboard())
        onView(withId(R.id.login_password))
                .perform(typeText(wrongPassword), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click());
        // Login using correct username + password combination
        onView(withText("The password is invalid or the user does not have a password."))
                .inRoot(withDecorView(not(rule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }


    @Test
    fun ensureLoginUsingEmailSuccess(){
        onView(withId(R.id.login_email))
                .perform(typeText(userEmail), closeSoftKeyboard())
        onView(withId(R.id.login_password))
                .perform(typeText(correctPassword), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click());
        // Login using correct username + password combination
        onView(withText("Succesfully logged in, welcome ${userEmail} !"))
                .inRoot(withDecorView(not(rule.activity.window.decorView)))
                .check(matches(isDisplayed()))
    }
}
