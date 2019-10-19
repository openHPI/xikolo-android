package de.xikolo.controllers.video

import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.github.rubensousa.previewseekbar.PreviewSeekBar
import com.github.rubensousa.previewseekbar.PreviewView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.FeatureConfig
import de.xikolo.controllers.base.BaseFragment
import de.xikolo.controllers.helper.VideoSettingsHelper
import de.xikolo.models.VideoStream
import de.xikolo.models.VideoSubtitles
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.extensions.ConnectivityType
import de.xikolo.utils.extensions.connectivityType
import de.xikolo.utils.extensions.isOnline
import de.xikolo.views.CustomFontTextView
import de.xikolo.views.CustomSizeVideoView
import de.xikolo.views.ExoPlayerVideoView
import java.util.*
import java.util.concurrent.TimeUnit

open class VideoStreamPlayerFragment(private var videoStream: VideoStream, private var autoPlay: Boolean? = true) : BaseFragment() {

    companion object {
        val TAG: String = VideoStreamPlayerFragment::class.java.simpleName

        private const val CONTROLS_FADE_DEFAULT_TIMEOUT = 3000
        private const val MESSAGE_CONTROLS_FADE_OUT = 1

        private val SEEKBAR_PREVIEW_HANDLER_TAG = "$TAG/seekBarPreview"
        private const val SEEKBAR_PREVIEW_INTERVAL = 100
        private const val SEEKBAR_PREVIEW_POSITION_DIFFERENCE = 5000

        private const val SEEKBAR_UPDATER_INTERVAL = 100L

        private const val VIDEO_STEPPING_DURATION = 10000
    }

    @BindView(R.id.playerView)
    lateinit var playerView: CustomSizeVideoView

    @BindView(R.id.shadowContainer)
    lateinit var shadowContainer: View

    @BindView(R.id.progressBar)
    lateinit var progressBar: View

    @BindView(R.id.interfaceContainer)
    lateinit var interfaceContainer: View

    @BindView(R.id.controlsContainer)
    lateinit var controlsContainer: View

    @BindView(R.id.settingsButton)
    lateinit var settingsButton: TextView

    @BindView(R.id.seekBar)
    lateinit var seekBar: PreviewSeekBar

    @BindView(R.id.seekBarPreviewLayout)
    lateinit var seekBarPreviewLayout: FrameLayout

    @BindView(R.id.seekBarPreviewImage)
    lateinit var seekBarPreviewImage: ImageView

    @BindView(R.id.playButton)
    lateinit var playButton: CustomFontTextView

    @BindView(R.id.stepForwardButton)
    lateinit var stepForwardButton: CustomFontTextView

    @BindView(R.id.stepBackwardButton)
    lateinit var stepBackwardButton: CustomFontTextView

    @BindView(R.id.retryButton)
    lateinit var retryButton: TextView

    @BindView(R.id.playbackTimeText)
    lateinit var playbackTimeText: TextView

    @BindView(R.id.totalTimeText)
    lateinit var totalTimeText: TextView

    @BindView(R.id.warningContainer)
    lateinit var warningContainer: View

    @BindView(R.id.warningText)
    lateinit var warningText: TextView

    @BindView(R.id.settingsContainer)
    lateinit var settingsContainer: ViewGroup

    private var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>? = null
    private lateinit var videoSettingsHelper: VideoSettingsHelper
    private var settingsOpen = false

    private var userIsSeeking = false
    private var seekBarUpdater: Runnable? = null
    private var seekBarUpdaterIsRunning = false
    private val seekBarPreviewThread: HandlerThread
    private val seekBarPreviewHandler: Handler

    protected var initialVideoPosition: Int = 0
    private var initialPlaybackState: Boolean = autoPlay ?: true
    private var isInitialPreparing = true

    private lateinit var controlsVisibilityHandler: ControlsVisibilityHandler

    private var isOfflineVideo = false

    private val applicationPreferences = ApplicationPreferences()

    val currentPlaybackSpeed: VideoSettingsHelper.PlaybackSpeed
        get() = videoSettingsHelper.currentSpeed

    val currentPosition: Int
        get() = playerView.currentPosition.toInt()

    val duration: Int
        get() = playerView.duration.toInt()

    private val hasEnded: Boolean
        get() = currentPosition >= duration

    val hasAlmostEnded: Boolean
        get() = currentPosition >= duration - 10000

    val isPlaying: Boolean
        get() = playerView.isPlaying()

    val sourceString: String
        get() = if (isOfflineVideo) "offline" else "online"

    val currentQualityString: String
        get() = videoSettingsHelper.currentQuality.name.toLowerCase()

    var isShowingControls: Boolean = false
        private set

    val controllerInterface: ControllerInterface?
        get() = activity as? ControllerInterface?

    init {
        seekBarPreviewThread = HandlerThread(SEEKBAR_PREVIEW_HANDLER_TAG)
        seekBarPreviewThread.start()
        seekBarPreviewHandler = Handler(seekBarPreviewThread.looper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        controlsVisibilityHandler = ControlsVisibilityHandler(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                detector?.let {
                    val immersive = it.scaleFactor > 1
                    videoSettingsHelper.isImmersiveModeEnabled = immersive
                    changeImmersiveMode(videoSettingsHelper.isImmersiveModeEnabled, immersive, true)
                }
            }
        })

        this.view?.setOnTouchListener { _, event ->
            if (!settingsOpen && controllerInterface?.isImmersiveModeAvailable() == true) {
                gestureDetector.onTouchEvent(event)
            }
            false
        }

        this.view?.setOnClickListener {
            if (settingsOpen) {
                hideSettings()
            }
            if (playerView.visibility == View.VISIBLE) {
                showControls()
            }
        }

        playerView.onPreparedListener = object : ExoPlayerVideoView.OnPreparedListener {
            override fun onPrepared() {
                hideProgress()

                playerView.visibility = View.VISIBLE
                warningContainer.visibility = View.GONE
                seekBar.max = duration

                seekTo(initialVideoPosition, false)

                totalTimeText.text = getTimeString(duration)
                playbackTimeText.text = getTimeString(currentPosition)

                stepForwardButton.visibility = View.VISIBLE
                stepBackwardButton.visibility = View.VISIBLE

                seekBar.isPreviewEnabled = playerView.previewAvailable

                if (initialPlaybackState) {
                    play(false)
                }
            }
        }

        playerView.onBufferUpdateListener = object : ExoPlayerVideoView.OnBufferUpdateListener {
            override fun onBufferingUpdate(percent: Int) {
                seekBar.secondaryProgress = (seekBar.max * (percent / 100.0)).toInt()
            }

            override fun onBufferingStart() {
                showProgress()
            }

            override fun onBufferingEnd() {
                hideProgress()
            }
        }

        playerView.onCompletionListener = object : ExoPlayerVideoView.OnCompletionListener {
            override fun onCompletion() {
                pause(false)
                playButton.text = getString(R.string.icon_reload)
                stepForwardButton.visibility = View.GONE
                stepBackwardButton.visibility = View.GONE
                showControls()
            }
        }

        playerView.onErrorListener = object : ExoPlayerVideoView.OnErrorListener {
            override fun onError(e: Exception?): Boolean {
                saveCurrentPosition()
                warningContainer.visibility = View.VISIBLE
                warningText.text = getString(R.string.error_plain)
                return true
            }
        }

        seekBarUpdater = object : Runnable {
            override fun run() {
                activity?.runOnUiThread {
                    seekBarUpdaterIsRunning = true

                    if (!userIsSeeking && isPlaying) {
                        seekBar.progress = currentPosition
                        playbackTimeText.text = getTimeString(currentPosition)
                    }

                    if (!hasEnded && isPlaying) {
                        Handler().postDelayed(
                            Thread(this),
                            SEEKBAR_UPDATER_INTERVAL
                        )
                    } else {
                        seekBarUpdaterIsRunning = false
                    }
                }
            }
        }

        playButton.setOnClickListener {
            showControls()
            if (isPlaying) {
                pause(true)
            } else {
                if (hasEnded) {
                    // 'replay' button was pressed
                    stepForwardButton.visibility = View.VISIBLE
                    stepBackwardButton.visibility = View.VISIBLE
                    seekTo(0, false)
                }

                play(true)
            }
        }

        settingsButton.setOnClickListener {
            showSettings(videoSettingsHelper.buildSettingsView())
        }

        stepForwardButton.setOnClickListener {
            showControls()
            stepForward()
        }

        stepBackwardButton.setOnClickListener {
            showControls()
            stepBackward()
        }

        seekBar.attachPreviewFrameLayout(seekBarPreviewLayout)
        seekBar.setPreviewLoader(object : com.github.rubensousa.previewseekbar.PreviewLoader {
            private var lastPreview: Long = 0
            private var lastPosition: Long = -1

            override fun loadPreview(currentPosition: Long, max: Long) {
                if (System.currentTimeMillis() - lastPreview > SEEKBAR_PREVIEW_INTERVAL && (lastPosition < 0 || Math.abs(currentPosition - lastPosition) > SEEKBAR_PREVIEW_POSITION_DIFFERENCE)) {
                    seekBarPreviewHandler.removeCallbacksAndMessages(null)
                    seekBarPreviewHandler.postAtFrontOfQueue {
                        val frame = playerView.getFrameAt(currentPosition)
                        activity?.runOnUiThread { seekBarPreviewImage.setImageBitmap(frame) }

                        lastPreview = System.currentTimeMillis()
                        lastPosition = currentPosition
                    }
                }
            }
        })
        seekBar.addOnPreviewChangeListener(object : PreviewView.OnPreviewChangeListener {
            override fun onStartPreview(previewView: PreviewView, progress: Int) {
                userIsSeeking = true
            }

            override fun onStopPreview(previewView: PreviewView, progress: Int) {
                seekBarPreviewHandler.removeCallbacksAndMessages(null)

                userIsSeeking = false
                seekTo(progress, true)
            }

            override fun onPreview(previewView: PreviewView, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    showControls()
                    playbackTimeText.text = getTimeString(progress)
                }
            }
        })

        retryButton.setOnClickListener {
            showProgress()
            updateVideo()
            seekTo(0, true)
            playerView.start()
            prepare()
        }

        controllerInterface?.onCreateSettings()?.let {
            settingsContainer = it
        }
        bottomSheetBehavior = BottomSheetBehavior.from(settingsContainer)
        bottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    settingsOpen = false
                    controllerInterface?.onSettingsClosed()
                    showControls()
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    settingsOpen = true
                    controllerInterface?.onSettingsOpened()
                    showControls(Integer.MAX_VALUE)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                controllerInterface?.onSettingsSliding(slideOffset)
            }
        })

        showProgress()
        setupVideo()
        updateVideo()
        if (autoPlay == true) {
            playerView.start()
        }
        prepare()
    }

    private fun setupVideo() {
        this.videoSettingsHelper = VideoSettingsHelper(
            activity!!,
            getSubtitleList(),
            object : VideoSettingsHelper.OnSettingsChangeListener {
                override fun onSubtitleChanged(old: VideoSubtitles?, new: VideoSubtitles?) {
                    hideSettings()
                    if (old != new) {
                        changeSubtitles(old, new, true)
                    }
                }

                override fun onQualityChanged(old: VideoSettingsHelper.VideoMode, new: VideoSettingsHelper.VideoMode) {
                    hideSettings()
                    if (old != new) {
                        changeQuality(old, new, true)
                    }
                }

                override fun onPlaybackSpeedChanged(old: VideoSettingsHelper.PlaybackSpeed, new: VideoSettingsHelper.PlaybackSpeed) {
                    hideSettings()
                    if (old != new) {
                        changePlaybackSpeed(old, new, true)
                    }
                }

                override fun onImmersiveModeChanged(old: Boolean, new: Boolean) {
                    hideSettings()
                    if (old != new) {
                        changeImmersiveMode(old, new, true)
                    }
                }
            },
            object : VideoSettingsHelper.OnSettingsClickListener {
                override fun onSubtitleClick() {
                    showSettings(videoSettingsHelper.buildSubtitleView())
                }

                override fun onPlaybackSpeedClick() {
                    showSettings(videoSettingsHelper.buildPlaybackSpeedView())
                }

                override fun onQualityClick() {
                    showSettings(videoSettingsHelper.buildQualityView())
                }

                override fun onPipClick() {
                    hideSettings()
                    controllerInterface?.onPipClicked()
                }
            },
            object : VideoSettingsHelper.VideoInfoCallback {
                override fun isOfflineAvailable(videoMode: VideoSettingsHelper.VideoMode): Boolean {
                    return getOfflineAvailability(videoMode)
                }

                override fun isImmersiveModeAvailable(): Boolean {
                    return controllerInterface?.isImmersiveModeAvailable() ?: false
                }
            }
        )
        videoSettingsHelper.currentQuality = getVideoMode()

        controllerInterface?.onImmersiveModeChanged(videoSettingsHelper.isImmersiveModeEnabled)
    }

    protected open fun getSubtitleList(): List<VideoSubtitles>? {
        return null
    }

    protected open fun getOfflineAvailability(videoMode: VideoSettingsHelper.VideoMode): Boolean {
        return false
    }

    protected open fun getVideoMode(): VideoSettingsHelper.VideoMode {
        return when {
            FeatureConfig.HLS_VIDEO && videoStream.hlsUrl != null        -> VideoSettingsHelper.VideoMode.AUTO
            context.connectivityType == ConnectivityType.WIFI
                || !applicationPreferences.isVideoQualityLimitedOnMobile -> VideoSettingsHelper.VideoMode.HD
            else                                                         -> VideoSettingsHelper.VideoMode.SD
        }
    }

    protected open fun getSubtitleUri(currentSubtitles: VideoSubtitles): String {
        return currentSubtitles.vttUrl
    }

    protected open fun getSubtitleLanguage(currentSubtitles: VideoSubtitles): String {
        return currentSubtitles.language
    }

    protected open fun setVideoUri(currentQuality: VideoSettingsHelper.VideoMode): Boolean {
        val stream: String
        val isHls: Boolean

        when (currentQuality) {
            VideoSettingsHelper.VideoMode.HD -> {
                stream = videoStream.hdUrl
                isHls = false
            }
            VideoSettingsHelper.VideoMode.SD -> {
                stream = videoStream.sdUrl
                isHls = false
            }
            else                             -> {
                stream = videoStream.hlsUrl
                isHls = true
            }
        }

        return when {
            context.isOnline                                                 -> { // device has internet connection
                if (isHls) {
                    setHlsVideoUri(stream)
                } else {
                    setVideoUri(stream)
                }
                true
            }
            currentQuality == VideoSettingsHelper.VideoMode.AUTO                   -> // retry with HD instead of HLS
                setVideoUri(VideoSettingsHelper.VideoMode.HD)
            videoSettingsHelper.currentQuality == VideoSettingsHelper.VideoMode.HD -> // retry with SD instead of HD
                setVideoUri(VideoSettingsHelper.VideoMode.SD)
            else                                                                   -> {
                warningContainer.visibility = View.VISIBLE
                warningText.text = getString(R.string.video_notification_no_offline_video)
                false
            }
        }
    }

    protected open fun changeSubtitles(oldSubtitles: VideoSubtitles?, newSubtitles: VideoSubtitles?, fromUser: Boolean) {
        showProgress()
        updateSubtitles()
        prepare()
    }

    protected open fun changeQuality(oldVideoMode: VideoSettingsHelper.VideoMode, newVideoMode: VideoSettingsHelper.VideoMode, fromUser: Boolean) {
        showProgress()
        updateVideo()
        prepare()
    }

    protected open fun changePlaybackSpeed(oldSpeed: VideoSettingsHelper.PlaybackSpeed, newSpeed: VideoSettingsHelper.PlaybackSpeed, fromUser: Boolean) {
        updatePlaybackSpeed()
    }

    protected open fun changeImmersiveMode(oldMode: Boolean, newMode: Boolean, fromUser: Boolean) {
        controllerInterface?.onImmersiveModeChanged(newMode)
    }

    private fun startSeekBarUpdater() {
        if (!seekBarUpdaterIsRunning) {
            Thread(seekBarUpdater).start()
        }
    }

    open fun play(fromUser: Boolean) {
        playButton.text = getString(R.string.icon_pause)
        startSeekBarUpdater()
        playerView.start()
    }

    open fun pause(fromUser: Boolean) {
        playButton.text = getString(R.string.icon_play)
        saveCurrentPosition()
        playerView.pause()
    }

    protected open fun seekTo(progress: Int, fromUser: Boolean) {
        playerView.seekTo(progress.toLong())
        playbackTimeText.text = getTimeString(progress)
        seekBar.progress = progress
        startSeekBarUpdater()
    }

    private fun stepForward() {
        seekTo(
            Math.min(
                currentPosition + VIDEO_STEPPING_DURATION,
                duration
            ),
            true
        )
    }

    private fun stepBackward() {
        seekTo(
            Math.max(
                currentPosition - VIDEO_STEPPING_DURATION,
                0
            ),
            true
        )
    }

    fun showSettings(view: View) {
        settingsContainer.removeAllViews()
        settingsContainer.addView(view)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideSettings() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showProgress() {
        playButton.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        playButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    fun showControls(timeout: Int = CONTROLS_FADE_DEFAULT_TIMEOUT) {
        isShowingControls = true
        controlsContainer.visibility = View.VISIBLE
        shadowContainer.visibility = View.VISIBLE
        controllerInterface?.onControlsShown()

        if (timeout != 0) {
            controlsVisibilityHandler.removeMessages(MESSAGE_CONTROLS_FADE_OUT)
            controlsVisibilityHandler.sendMessageDelayed(
                controlsVisibilityHandler.obtainMessage(MESSAGE_CONTROLS_FADE_OUT),
                timeout.toLong()
            )
        }
    }

    fun hideControls() {
        isShowingControls = false
        controlsContainer.visibility = View.GONE
        shadowContainer.visibility = View.GONE
        controllerInterface?.onControlsHidden()
    }

    fun setControlsPadding(left: Int?, top: Int?, right: Int?, bottom: Int?) {
        if (view != null) {
            interfaceContainer.setPadding(
                left ?: 0,
                top ?: 0,
                right ?: 0,
                bottom ?: 0
            )
        }
    }

    override fun onPause() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            pause(false)
        }
        super.onPause()
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            pause(false)
        }
        super.onStop()
    }

    override fun onDestroy() {
        playerView.release()
        seekBarPreviewThread.quit()
        super.onDestroy()
    }

    fun handleBackPress(): Boolean {
        if (settingsOpen) {
            hideSettings()
            return false
        }
        return true
    }

    private fun updatePlaybackSpeed() {
        playerView.setPlaybackSpeed(currentPlaybackSpeed.value)
    }

    private fun updateSubtitles() {
        val currentSubtitles = videoSettingsHelper.currentVideoSubtitles
        if (currentSubtitles != null) {
            playerView.showSubtitles(getSubtitleUri(currentSubtitles), getSubtitleLanguage(currentSubtitles))
        } else {
            playerView.removeSubtitles()
        }
    }

    private fun updateVideo() {
        warningContainer.visibility = View.GONE

        if (setVideoUri(videoSettingsHelper.currentQuality)) {
            updateSubtitles()
            updatePlaybackSpeed()
            if (isOfflineVideo) {
                playerView.uri?.let {
                    playerView.setPreviewUri(it)
                }
            } else if (context.isOnline) {
                if (videoStream.sdUrl != null) {
                    playerView.setPreviewUri(Uri.parse(videoStream.sdUrl))
                } else if (videoStream.hdUrl != null) {
                    playerView.setPreviewUri(Uri.parse(videoStream.hdUrl))
                }
            }
        }
    }

    protected fun setLocalVideoUri(localUri: String) {
        setVideoUri(localUri)
        isOfflineVideo = true
    }

    private fun setHlsVideoUri(hlsUri: String) {
        if (Config.DEBUG) {
            Log.i(TAG, "HLS Video HOST_URL: $hlsUri")
        }
        playerView.setVideoURI(Uri.parse(hlsUri), true)
        isOfflineVideo = false
    }

    private fun setVideoUri(uri: String) {
        if (Config.DEBUG) {
            Log.i(TAG, "Video HOST_URL: $uri")
        }
        playerView.setVideoURI(Uri.parse(uri), false)
        isOfflineVideo = false
    }

    private fun prepare() {
        initialPlaybackState = isPlaying
        if (!isInitialPreparing) {
            initialVideoPosition = currentPosition
        }
        playerView.pause()
        playerView.prepare()
        isInitialPreparing = false
    }

    protected open fun saveCurrentPosition() {}

    private fun getTimeString(millis: Int): String {
        return String.format(Locale.US, "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(millis.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis.toLong()))
        )
    }

    interface ControllerInterface {

        fun onControlsShown()

        fun onControlsHidden()

        fun onCreateSettings(): ViewGroup? {
            return null
        }

        fun onSettingsSliding(offset: Float)

        fun onSettingsOpened()

        fun onSettingsClosed()

        fun onPipClicked()

        fun onImmersiveModeChanged(enabled: Boolean)

        fun isImmersiveModeAvailable(): Boolean {
            return false
        }
    }

    protected class ControlsVisibilityHandler(private val controller: VideoStreamPlayerFragment) : Handler() {

        override fun handleMessage(message: Message) {
            when (message.what) {
                MESSAGE_CONTROLS_FADE_OUT ->
                    if (controller.isPlaying) {
                        controller.hideControls()
                    }
            }
        }
    }

}
