/*
# COMP 4521 # IYER ARVIND   20218689    aiyer@connect.ust.hk
# COMP 4521 # RYAN BUDI     20214841    bryanaa@connect.ust.hk
# COMP 4521 # JI TONY WAI SUM 20217881  twsji@connect.ust.hk
*/
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
import android.support.test.uiautomator.UiDevice
import org.junit.Before


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

}
