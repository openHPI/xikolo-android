package de.xikolo.testing.instrumented.ui


import android.content.pm.ActivityInfo
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.R
import de.xikolo.controllers.video.VideoStreamPlayerActivity
import de.xikolo.controllers.video.VideoStreamPlayerActivityAutoBundle
import de.xikolo.controllers.video.VideoStreamPlayerFragment
import de.xikolo.testing.instrumented.mocking.SingleObjects
import de.xikolo.testing.instrumented.mocking.base.BaseMockedTest
import de.xikolo.testing.instrumented.ui.helper.NavigationHelper.WAIT_UI_ANIMATION
import de.xikolo.testing.instrumented.ui.helper.NavigationHelper.WAIT_UI_LONG
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class VideoTest : BaseMockedTest() {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(VideoStreamPlayerActivity::class.java, false, false)

    private lateinit var activity: VideoStreamPlayerActivity
    private lateinit var fragment: VideoStreamPlayerFragment

    @Before
    fun startActivity() {
        activityTestRule.launchActivity(
            VideoStreamPlayerActivityAutoBundle.builder(SingleObjects.testVideoStream).build(context)
        )

        activity = activityTestRule.activity as VideoStreamPlayerActivity
        fragment = activity.playerFragment
    }

    private fun waitForVideo() {
        while (fragment.playerView.visibility != View.VISIBLE) {
            Thread.sleep(WAIT_UI_LONG)
        }
    }

    private fun waitForProgress() {
        while (fragment.progressBar.visibility == View.VISIBLE) {
            Thread.sleep(WAIT_UI_LONG)
        }
    }

    private fun showControls() {
        activity.runOnUiThread {
            fragment.showControls()
        }
    }

    @Test
    fun testControlsUI() {
        waitForVideo()

        val ui = onView(
            allOf(
                withId(R.id.content_view)
            )
        )

        val controls = onView(
            allOf(
                withId(R.id.controlsContainer)
            )
        )

        ui.perform(
            click()
        )

        controls.check(
            matches(isDisplayed())
        )
    }

    @Test
    fun testControlsEffect() {
        waitForVideo()

        activity.runOnUiThread {
            fragment.showControls()
            assertTrue(fragment.isShowingControls)
        }

        activity.runOnUiThread {
            fragment.hideControls()
            assertFalse(fragment.isShowingControls)
        }
    }

    @Test
    fun testInitialProgress() {
        val progressView = onView(
            allOf(
                withId(R.id.progressBar)
            )
        )

        progressView.check(
            matches(isDisplayed())
        )
    }

    @Test
    fun testProgress() {
        waitForVideo()

        val progressView = onView(
            allOf(
                withId(R.id.progressBar)
            )
        )

        activity.runOnUiThread {
            fragment.showProgress()
        }

        progressView.check(
            matches(isDisplayed())
        )

        activity.runOnUiThread {
            fragment.hideProgress()
        }

        progressView.check(
            matches(not(isDisplayed()))
        )
    }

    @Test
    fun testPlayAndPauseUI() {
        waitForVideo()
        showControls()

        fragment.pause(false)

        val playButton = onView(
            allOf(
                withId(R.id.playButton)
            )
        )

        playButton.perform(
            click()
        )
        playButton.check(
            matches(
                withText(R.string.icon_pause)
            )
        )

        playButton.perform(
            click()
        )
        playButton.check(
            matches(withText(R.string.icon_play))
        )
    }

    @Test
    fun testPlayAndPauseEffect() {
        waitForVideo()

        fragment.pause(false)
        assertFalse(fragment.isPlaying)

        fragment.play(false)
        assertTrue(fragment.isPlaying)
    }

    @Test
    fun testReplayButton() {
        waitForVideo()

        fragment.playerView.seekTo(fragment.playerView.duration - 100)
        waitForProgress()
        Thread.sleep(3000) // wait for player to finish

        val playButton = onView(
            allOf(
                withId(R.id.playButton)
            )
        )

        playButton.check(
            matches(
                withText(R.string.icon_reload)
            )
        )

        val prePosition = fragment.currentPosition
        playButton.perform(
            click()
        )
        waitForProgress()
        val postPosition = fragment.currentPosition

        assertTrue(postPosition < prePosition)

        playButton.check(
            matches(
                withText(R.string.icon_pause)
            )
        )
    }

    @Test
    fun testSeeking() {
        waitForVideo()
        showControls()

        val seekBar = onView(
            allOf(
                withId(R.id.seekBar)
            )
        )

        val prePosition = fragment.currentPosition
        seekBar.perform(
            swipeRight()
        )
        val postPosition = fragment.currentPosition
        assertTrue(postPosition > prePosition)
    }

    @Test
    fun testSeekBarUpdating() {
        waitForVideo()

        fragment.play(false)

        val prePosition = fragment.seekBar.progress
        Thread.sleep(2000) // let video run a bit
        val postPosition = fragment.seekBar.progress

        assertTrue(postPosition > prePosition)
    }

    @Test
    fun testStepping() {
        waitForVideo()
        showControls()

        val stepForward = onView(
            allOf(
                withId(R.id.stepForwardButton)
            )
        )

        val stepBackward = onView(
            allOf(
                withId(R.id.stepBackwardButton)
            )
        )

        val prePositionForward = fragment.currentPosition
        stepForward.perform(
            click()
        )
        val postPositionForward = fragment.currentPosition
        assertTrue(postPositionForward > prePositionForward)

        val prePositionBackward = fragment.currentPosition
        stepBackward.perform(
            click()
        )
        val postPositionBackward = fragment.currentPosition
        assertTrue(prePositionBackward > postPositionBackward)
    }

    @Test
    fun testRotation() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        waitForVideo()

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    @Test
    fun testSettingsButton() {
        waitForVideo()
        showControls()

        val settingsButton = onView(
            allOf(
                withId(R.id.settingsButton)
            )
        )

        settingsButton.perform(
            click()
        )

        val settingsContainer = onView(
            allOf(
                withId(R.id.settingsContainer),
                isDisplayed()
            )
        )

        settingsContainer.check(
            matches(
                not(hasChildCount(0))
            )
        )

        settingsContainer.perform(
            swipeDown()
        )
    }

    @Test
    fun testBackBehavior() {
        waitForVideo()
        showControls()

        val settingsButton = onView(
            allOf(
                withId(R.id.settingsButton)
            )
        )

        settingsButton.perform(
            click()
        )

        Thread.sleep(WAIT_UI_ANIMATION) // wait for panel to open

        val firstBackPress = fragment.handleBackPress()
        assertFalse(firstBackPress)

        Thread.sleep(WAIT_UI_ANIMATION) // wait for panel to close

        val secondBackPress = fragment.handleBackPress()
        assertTrue(secondBackPress)
    }

}
