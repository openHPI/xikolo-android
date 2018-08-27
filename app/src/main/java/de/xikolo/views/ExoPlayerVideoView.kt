package de.xikolo.views

import android.content.Context
import android.net.Uri
import android.support.annotation.IntRange
import android.util.AttributeSet
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

open class ExoPlayerVideoView : PlayerView {
    private lateinit var mContext: Context

    private lateinit var mPlayer: SimpleExoPlayer
    private val mBandwidthMeter: DefaultBandwidthMeter = DefaultBandwidthMeter()
    private lateinit var mPlayerListener: Player.EventListener
    private var mMediaSource: MediaSource? = null

    public var onPreparedListener: OnPreparedListener? = null

    public var onBufferUpdateListener: OnBufferUpdateListener? = null

    public var onCompletionListener: OnCompletionListener? = null

    public var onErrorListener: OnErrorListener? = null

    var isPreparing = false;

    val duration: Long
        get() = player.duration

    val currentPosition: Long
        get() = player.currentPosition

    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup(context, attrs)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        mContext = context
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(mBandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        mPlayerListener = object : Player.EventListener {
            override fun onLoadingChanged(isLoading: Boolean) {
                onBufferUpdateListener?.onBufferingUpdate(mPlayer.bufferedPercentage)
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && isPreparing) {
                    onPreparedListener?.onPrepared()
                    isPreparing = false
                }

                if (playbackState == Player.STATE_ENDED) {
                    onCompletionListener?.onCompletion()
                    player.seekTo(0)
                }
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                onErrorListener?.onError(error)
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            }

            override fun onSeekProcessed() {
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            }

            override fun onPositionDiscontinuity(reason: Int) {
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            }
        }
        mPlayer.addListener(mPlayerListener)

        player = mPlayer

        this.useController = false
    }

    fun start() {
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(
            speed,
            player.playbackParameters.pitch,
            player.playbackParameters.skipSilence)
    }

    fun setVideoURI(uri: Uri) {
        val dataSourceFactory = DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, mContext.packageName), mBandwidthMeter)
        mMediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        prepare()
    }

    fun prepare() {
        isPreparing = true
        mPlayer.prepare(mMediaSource)
    }

    fun release() {
        player.release()
    }

    fun isPlaying(): Boolean {
        return player.playWhenReady
    }

    interface OnPreparedListener {
        fun onPrepared()
    }

    interface OnBufferUpdateListener {
        fun onBufferingUpdate(@IntRange(from = 0L, to = 100L) percent: Int)
    }

    interface OnCompletionListener {
        fun onCompletion()
    }

    interface OnErrorListener {
        fun onError(e: Exception?): Boolean
    }
}