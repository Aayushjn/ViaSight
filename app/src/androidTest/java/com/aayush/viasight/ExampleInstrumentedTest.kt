package com.aayush.viasight

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aayush.viasight.util.android.getContacts
import com.aayush.viasight.util.android.getInstalledApps
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun useAppContext() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun appList_listIsNotEmpty_returnsList() {
        assertNotEquals(context.packageManager.getInstalledApps().size, 0)
    }

    @Test
    fun contactList_listIsNotEmpty_returnsList() {
        assertNotEquals(context.contentResolver.getContacts().size, 0)
    }
}
