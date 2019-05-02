package de.xikolo.mocking.base

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry

open class BaseTest {

    val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}
