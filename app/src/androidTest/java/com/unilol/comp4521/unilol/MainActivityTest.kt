package com.unilol.comp4521.unilol

import android.graphics.Point
import android.os.RemoteException
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by arvind on 23/5/18.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)
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
        Assert.assertEquals("com.unilol.comp4521.unilol", appContext.packageName)
    }

    @Test
    fun verifyIfLoggedIn() {
        val auth = MainActivity::class.java.getDeclaredField("mAuth") as FirebaseAuth
        Assert.assertNotEquals(null, auth.currentUser)
    }
}