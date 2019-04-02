package com.aayush.viasight

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aayush.viasight.util.getContacts
import com.aayush.viasight.util.getInstalledApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.aayush.viasight", appContext.packageName)
    }

    @Test
    fun appList_listIsNotEmpty_returnsList() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotEquals(getInstalledApps(context.packageManager).size, 0)
    }

    @Test
    fun contactList_listIsNotEmpty_returnsList() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotEquals(getContacts(context.contentResolver).size, 0)
    }
}
