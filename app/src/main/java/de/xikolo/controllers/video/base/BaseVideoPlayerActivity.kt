package de.xikolo.controllers.video.base

import android.annotation.TargetApi
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.NavUtils
import butterknife.BindView
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.FeatureConfig
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.video.VideoStreamPlayerFragment
import de.xikolo.utils.DisplayUtil
import de.xikolo.utils.extensions.showToast
import java.util.*

abstract class BaseVideoPlayerActivity : BaseActivity(), VideoStreamPlayerFragment.ControllerInterface {

    companion object {
        val TAG: String = BaseVideoPlayerActivity::class.java.simpleName

        const val ACTION_SWITCH_PLAYBACK_STATE = "switch_playback_state"
    }

    @BindView(R.id.container)
    lateinit var container: ViewGroup

    @BindView(R.id.settingsContainer)
    lateinit var settingsContainer: ViewGroup

    @BindView(R.id.screenOverlay)
    lateinit var screenOverlay: View

    @BindView(R.id.videoPlayerFragment)
    lateinit var playerFragmentContainer: ViewGroup

    open var parentIntent: Intent? = null
    open var overrideActualParent: Boolean = false

    private var pipControlsBroadcastReceiver: BroadcastReceiver? = null
    private var isBackStackLost = false
    private var hasDisplayCutout = false
    private var isInImmersiveMode = false

    lateinit var playerFragment: VideoStreamPlayerFragment

    open val layoutResource = R.layout.activity_video

    abstract fun createPlayerFragment(): VideoStreamPlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY)

        setContentView(layoutResource)

        setupActionBar()
        enableOfflineModeToolbar(false)
        setColorScheme(R.color.transparent, R.color.black)
        actionBar.title = ""
        actionBar.subtitle = ""

        updatePlayerFragment()
    }

    private fun updatePlayerFragment() {
        playerFragment = createPlayerFragment()

        val fragmentTag = playerFragment.hashCode().toString()
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(fragmentTag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.videoPlayerFragment, playerFragment, fragmentTag)
            transaction.commit()
        }
    }

    open fun updateVideoView(orientation: Int = resources.configuration.orientation) {
        if (isInLandscape(orientation)) {
            hideSystemBars()
            if (isInImmersiveMode && isImmersiveModeAvailable()) {
                enableImmersiveMode()
            } else {
                disableImmersiveMode()
            }
            if (!playerFragment.isShowingControls) {
                actionBar.hide()
            }
        } else {
            disableImmersiveMode()
            showSystemBars()
            actionBar.show()
        }
    }

    override fun onControlsShown() {
        actionBar.show()
    }

    override fun onControlsHidden() {
        if (isInLandscape()) {
            actionBar.hide()
        } else {
            actionBar.show()
        }
    }

    override fun onCreateSettings(): ViewGroup? {
        return settingsContainer
    }

    override fun onSettingsSliding(offset: Float) {
        val alpha = offset * 0.7f
        if (!alpha.isNaN()) {
            screenOverlay.alpha = alpha
        }
    }

    override fun onSettingsOpened() {
        screenOverlay.isClickable = true
        screenOverlay.setOnClickListener { playerFragment.hideSettings() }
    }

    override fun onSettingsClosed() {
        screenOverlay.setOnClickListener(null)
        screenOverlay.isClickable = false
    }

    override fun onPipClicked() {
        enterPip(true)
    }

    override fun onImmersiveModeChanged(enabled: Boolean) {
        isInImmersiveMode = enabled
        updateVideoView()
    }

    override fun isImmersiveModeAvailable(): Boolean {
        return hasDisplayCutout || DisplayUtil.getAspectRatio(this) - playerFragment.playerView.aspectRatio > 0.01
    }

    @TargetApi(26)
    private fun enterPip(userInteraction: Boolean) {
        if (!enterPictureInPictureMode(getPipParams(playerFragment.isPlaying)) && userInteraction) {
            showToast(R.string.toast_pip_error)
        }
    }

    @TargetApi(26)
    private fun getPipParams(playing: Boolean): PictureInPictureParams {
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_SWITCH_PLAYBACK_STATE),
            0)

        val actionList = ArrayList<RemoteAction>()
        actionList.add(
            RemoteAction(
                Icon.createWithResource(
                    this,
                    if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play),
                if (playing) getString(R.string.video_pip_action_pause) else getString(R.string.video_pip_action_play),
                if (playing) getString(R.string.video_pip_action_pause) else getString(R.string.video_pip_action_play),
                pendingIntent
            )
        )

        val pipBounds = Rect()
        pipBounds.set(
            playerFragment.playerView.left,
            playerFragment.playerView.top,
            playerFragment.playerView.right,
            playerFragment.playerView.bottom
        )

        return PictureInPictureParams.Builder()
            .setSourceRectHint(pipBounds)
            .setActions(actionList)
            .build()
    }

    @TargetApi(26)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        if (isInPictureInPictureMode) {
            playerFragment.hideSettings()
            playerFragment.hideControls()
            actionBar.hide()
            disableImmersiveMode()

            pipControlsBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    if (intent?.action != ACTION_SWITCH_PLAYBACK_STATE) {
                        return
                    }

                    if (playerFragment.isPlaying) {
                        playerFragment.pause(true)
                    } else {
                        playerFragment.play(true)
                    }
                    setPictureInPictureParams(getPipParams(playerFragment.isPlaying))
                }
            }
            registerReceiver(pipControlsBroadcastReceiver, IntentFilter(ACTION_SWITCH_PLAYBACK_STATE))
        } else {
            updateVideoView()
            playerFragment.showControls()

            try {
                unregisterReceiver(pipControlsBroadcastReceiver)
            } catch (e: IllegalArgumentException) {
                if (Config.DEBUG) {
                    Log.w(TAG, "Could not unregister PiP playback control BroadcastReceiver: $e")
                }
            }

            pipControlsBroadcastReceiver = null
            isBackStackLost = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        hasDisplayCutout = DisplayUtil.hasDisplayCutouts(this)
    }

    override fun onStart() {
        super.onStart()
        updateVideoView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            updatePlayerFragment()
        }
    }

    private fun hideSystemBars() {
        window.decorView.systemUiVisibility =
            if (isInLandscape()) {
                (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                View.SYSTEM_UI_FLAG_VISIBLE
            }
    }

    private fun showSystemBars() {
        window.decorView.systemUiVisibility =
            if (isInLandscape()) {
                (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            } else {
                View.SYSTEM_UI_FLAG_VISIBLE
            }
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                    settingsContainer.setPadding(
                        insets.displayCutout?.safeInsetLeft ?: 0,
                        0,
                        insets.displayCutout?.safeInsetRight ?: 0,
                        0
                    )
                    playerFragment.setControlsPadding(
                        insets.displayCutout?.safeInsetLeft ?: 0,
                        0,
                        insets.displayCutout?.safeInsetRight ?: 0,
                        0
                    )
                    insets
                }
            }

            val attributes = window.attributes
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = attributes
        }

        playerFragment.playerView.scaleToFill()
    }

    private fun disableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            settingsContainer.setPadding(0, 0, 0, 0)
            playerFragment.setControlsPadding(0, 0, 0, 0)
            window.decorView.setOnApplyWindowInsetsListener(null)

            val attributes = window.attributes
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            window.attributes = attributes
        }

        playerFragment.playerView.scaleToFit()
    }

    private fun navigateUp() {
        if ((isBackStackLost || overrideActualParent) && parentIntent != null) {
            finishAndRemoveTask()
            parentIntent?.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(parentIntent)
        } else {
            NavUtils.navigateUpFromSameTask(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (playerFragment.handleBackPress()) {
            if (isBackStackLost) {
                navigateUp()
            } else {
                super.onBackPressed()
            }
        }
    }

    public override fun onUserLeaveHint() {
        if (FeatureConfig.PIP && !playerFragment.hasAlmostEnded) {
            super.onUserLeaveHint()
            enterPip(false)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateVideoView(newConfig.orientation)
    }

    protected fun isInLandscape(orientation: Int = resources.configuration.orientation): Boolean =
        orientation == Configuration.ORIENTATION_LANDSCAPE

}
