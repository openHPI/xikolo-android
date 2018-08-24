package de.xikolo.views

import android.content.Context
import android.net.Uri
import android.support.annotation.IntRange
import android.util.AttributeSet
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
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

    public var onPreparedListener: OnPreparedListener? = null

    public var onBufferUpdateListener: OnBufferUpdateListener? = null

    public var onCompletionListener: OnCompletionListener? = null

    public var onErrorListener: OnErrorListener? = null

    val duration: Long
        get() = mPlayer.duration

    val currentPosition: Long
        get() = mPlayer.currentPosition

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
                if (playbackState == Player.STATE_READY)
                    onPreparedListener?.onPrepared()
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
        mPlayer.playWhenReady = true
    }

    fun pause() {
        mPlayer.playWhenReady = false
    }

    fun seekTo(position: Long) {
        mPlayer.seekTo(position)
    }

    fun setPlaybackSpeed(speed: Float) {
        mPlayer.playbackParameters = PlaybackParameters(
            speed,
            mPlayer.playbackParameters.pitch,
            mPlayer.playbackParameters.skipSilence)
    }

    fun setVideoURI(uri: Uri) {
        val dataSourceFactory = DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, mContext.packageName), mBandwidthMeter)
        val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        mPlayer.prepare(mediaSource)
        mPlayer.
    }

    fun release() {
        mPlayer.release()
    }

    fun isPlaying(): Boolean {
        return mPlayer.playWhenReady
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