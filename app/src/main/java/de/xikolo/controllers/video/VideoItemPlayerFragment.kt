package de.xikolo.controllers.video

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.mediarouter.app.MediaRouteButton
import butterknife.BindView
import com.google.android.gms.cast.framework.CastButtonFactory
import de.xikolo.R
import de.xikolo.controllers.helper.VideoSettingsHelper
import de.xikolo.managers.DownloadManager
import de.xikolo.models.DownloadAsset
import de.xikolo.models.Item
import de.xikolo.models.Video
import de.xikolo.models.VideoSubtitles
import de.xikolo.models.dao.ItemDao
import de.xikolo.models.dao.VideoDao
import de.xikolo.utils.CastUtil
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.PlayServicesUtil
import de.xikolo.utils.PlaybackSpeedUtil

class VideoItemPlayerFragment(private var courseId: String, private var sectionId: String, private var itemId: String, videoId: String, autoPlay: Boolean? = true) : VideoStreamPlayerFragment(VideoDao.Unmanaged.find(videoId)!!.singleStream, autoPlay) {

    companion object {
        val TAG: String = VideoItemPlayerFragment::class.java.simpleName
    }

    @BindView(R.id.video_media_route_button)
    lateinit var mediaRouteButton: MediaRouteButton

    private var video: Video = VideoDao.Unmanaged.find(videoId)!!
    private var item: Item = ItemDao.Unmanaged.find(itemId)!!

    private var videoDownloadAssetHD: DownloadAsset.Course.Item.VideoHD = DownloadAsset.Course.Item.VideoHD(item, video)
    private var videoDownloadAssetSD: DownloadAsset.Course.Item.VideoSD = DownloadAsset.Course.Item.VideoSD(item, video)

    private lateinit var downloadManager: DownloadManager

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        downloadManager = DownloadManager(activity!!)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            if (PlayServicesUtil.checkPlayServices(it)) {
                CastButtonFactory.setUpMediaRouteButton(it, mediaRouteButton)
            }

            updateCastButton()
        }
    }

    fun updateCastButton() {
        if (view != null) {
            mediaRouteButton.visibility =
                if (CastUtil.isAvailable() && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    override fun play(fromUser: Boolean) {
        if (fromUser) {
            LanalyticsUtil.trackVideoPlay(itemId,
                courseId, sectionId,
                currentPosition,
                currentPlaybackSpeed.speed,
                activity!!.resources.configuration.orientation,
                currentQualityString,
                sourceString)
        }
        super.play(fromUser)
    }

    override fun pause(fromUser: Boolean) {
        if (fromUser) {
            LanalyticsUtil.trackVideoPause(itemId,
                courseId, sectionId,
                currentPosition,
                currentPlaybackSpeed.speed,
                activity!!.resources.configuration.orientation,
                currentQualityString,
                sourceString)
        }
        super.pause(fromUser)
    }

    override fun seekTo(progress: Int, fromUser: Boolean) {
        if (fromUser) {
            LanalyticsUtil.trackVideoSeek(itemId,
                courseId, sectionId,
                currentPosition,
                progress,
                currentPlaybackSpeed.speed,
                activity!!.resources.configuration.orientation,
                currentQualityString,
                sourceString)
        }
        super.seekTo(progress, fromUser)
    }

    override fun changeQuality(oldVideoMode: VideoSettingsHelper.VideoMode, newVideoMode: VideoSettingsHelper.VideoMode, fromUser: Boolean) {
        if (fromUser) {
            val oldSourceString = sourceString
            super.changeQuality(oldVideoMode, newVideoMode, true)
            LanalyticsUtil.trackVideoChangeQuality(itemId,
                courseId, sectionId,
                currentPosition,
                currentPlaybackSpeed.speed,
                activity!!.resources.configuration.orientation,
                getQualityString(oldVideoMode),
                getQualityString(newVideoMode),
                oldSourceString,
                sourceString)
        } else {
            super.changeQuality(oldVideoMode, newVideoMode, false)
        }
    }

    override fun changePlaybackSpeed(oldSpeed: PlaybackSpeedUtil, newSpeed: PlaybackSpeedUtil, fromUser: Boolean) {
        super.changePlaybackSpeed(oldSpeed, newSpeed, fromUser)
        if (fromUser) {
            LanalyticsUtil.trackVideoChangeSpeed(itemId,
                courseId, sectionId,
                currentPosition,
                oldSpeed.speed,
                newSpeed.speed,
                activity!!.resources.configuration.orientation,
                currentQualityString,
                sourceString)
        }
    }

    override fun saveCurrentPosition() {
        super.saveCurrentPosition()
        video.progress = currentPosition
    }

    override fun getSubtitleList(): List<VideoSubtitles>? {
        return video.subtitles
    }

    override fun getSubtitleUri(currentSubtitles: VideoSubtitles): String {
        val downloadAsset = DownloadAsset.Course.Item.Subtitles(currentSubtitles, item)
        return if (downloadManager.downloadExists(downloadAsset)) {
            "file://" + downloadManager.getDownloadFile(downloadAsset)!!.absolutePath
        } else {
            super.getSubtitleUri(currentSubtitles)
        }
    }

    override fun setVideoUri(currentQuality: VideoSettingsHelper.VideoMode): Boolean {
        val videoAssetDownload: DownloadAsset.Course.Item?

        when (currentQuality) {
            VideoSettingsHelper.VideoMode.HD -> videoAssetDownload = videoDownloadAssetHD
            VideoSettingsHelper.VideoMode.SD -> videoAssetDownload = videoDownloadAssetSD
            else                             -> videoAssetDownload = null
        }

        return if (videoAssetDownload != null && videoDownloadPresent(videoAssetDownload)) {
            setLocalVideoUri("file://" + downloadManager.getDownloadFile(videoAssetDownload)!!)
            true
        } else {
            super.setVideoUri(currentQuality)
        }
    }

    override fun getVideoMode(): VideoSettingsHelper.VideoMode {
        return when {
            videoDownloadPresent(videoDownloadAssetHD) -> // hd video download available
                VideoSettingsHelper.VideoMode.HD
            videoDownloadPresent(videoDownloadAssetSD) -> // sd video download available
                VideoSettingsHelper.VideoMode.SD
            else                                       -> super.getVideoMode()
        }
    }

    override fun getOfflineAvailability(videoMode: VideoSettingsHelper.VideoMode): Boolean {
        return when (videoMode) {
            VideoSettingsHelper.VideoMode.HD -> videoDownloadPresent(videoDownloadAssetHD)
            VideoSettingsHelper.VideoMode.SD -> videoDownloadPresent(videoDownloadAssetSD)
            else                             -> false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        LanalyticsUtil.trackVideoChangeOrientation(itemId,
            courseId, sectionId,
            currentPosition,
            currentPlaybackSpeed.speed,
            newConfig!!.orientation,
            currentQualityString,
            sourceString)
    }

    private fun videoDownloadPresent(item: DownloadAsset.Course.Item): Boolean {
        return !downloadManager.downloadRunning(item) && downloadManager.downloadExists(item)
    }

    private fun getQualityString(videoMode: VideoSettingsHelper.VideoMode): String {
        return videoMode.name.toLowerCase()
    }

}
