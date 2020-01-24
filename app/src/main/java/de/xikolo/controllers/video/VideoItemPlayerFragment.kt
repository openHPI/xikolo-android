package de.xikolo.controllers.video

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.controllers.helper.VideoSettingsHelper
import de.xikolo.managers.DownloadManager
import de.xikolo.models.*
import de.xikolo.models.dao.ItemDao
import de.xikolo.models.dao.VideoDao
import de.xikolo.utils.LanalyticsUtil
import io.realm.Realm
import java.util.*
import kotlin.math.max

class VideoItemPlayerFragment : VideoStreamPlayerFragment() {

    companion object {
        val TAG: String = VideoItemPlayerFragment::class.java.simpleName

        private const val VIDEO_POSITION_REWIND_TIME = 10000
    }

    @AutoBundleField
    lateinit var courseId: String

    @AutoBundleField
    lateinit var sectionId: String

    @AutoBundleField
    lateinit var itemId: String

    @AutoBundleField
    lateinit var videoId: String

    override val videoStream: VideoStream
        get() = video.streamToPlay

    private val video: Video
        get() = VideoDao.Unmanaged.find(videoId)!!

    private val item: Item
        get() = ItemDao.Unmanaged.find(itemId)!!

    private val videoDownloadAssetHD: DownloadAsset.Course.Item.VideoHD
        get() = DownloadAsset.Course.Item.VideoHD(item, video)

    private val videoDownloadAssetSD: DownloadAsset.Course.Item.VideoSD
        get() = DownloadAsset.Course.Item.VideoSD(item, video)

    private lateinit var downloadManager: DownloadManager

    private val videoDao = VideoDao(Realm.getDefaultInstance())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        downloadManager = DownloadManager(activity!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialVideoPosition = max(video.progress - VIDEO_POSITION_REWIND_TIME, 0)
    }

    override fun play(fromUser: Boolean) {
        if (fromUser) {
            LanalyticsUtil.trackVideoPlay(itemId,
                courseId, sectionId,
                currentPosition,
                currentPlaybackSpeed.value,
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
                currentPlaybackSpeed.value,
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
                currentPlaybackSpeed.value,
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
                currentPlaybackSpeed.value,
                activity!!.resources.configuration.orientation,
                getQualityString(oldVideoMode),
                getQualityString(newVideoMode),
                oldSourceString,
                sourceString)
        } else {
            super.changeQuality(oldVideoMode, newVideoMode, false)
        }
    }

    override fun changePlaybackSpeed(oldSpeed: VideoSettingsHelper.PlaybackSpeed, newSpeed: VideoSettingsHelper.PlaybackSpeed, fromUser: Boolean) {
        super.changePlaybackSpeed(oldSpeed, newSpeed, fromUser)
        if (fromUser) {
            LanalyticsUtil.trackVideoChangeSpeed(itemId,
                courseId, sectionId,
                currentPosition,
                oldSpeed.value,
                newSpeed.value,
                activity!!.resources.configuration.orientation,
                currentQualityString,
                sourceString)
        }
    }

    override fun saveCurrentPosition() {
        super.saveCurrentPosition()
        videoDao.updateProgress(video, currentPosition)
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
        val videoAssetDownload: DownloadAsset.Course.Item? = when (currentQuality) {
            VideoSettingsHelper.VideoMode.HD -> videoDownloadAssetHD
            VideoSettingsHelper.VideoMode.SD -> videoDownloadAssetSD
            else                             -> null
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        LanalyticsUtil.trackVideoChangeOrientation(itemId,
            courseId, sectionId,
            currentPosition,
            currentPlaybackSpeed.value,
            newConfig.orientation,
            currentQualityString,
            sourceString)
    }

    private fun videoDownloadPresent(item: DownloadAsset.Course.Item): Boolean {
        return !downloadManager.downloadRunning(item) && downloadManager.downloadExists(item)
    }

    private fun getQualityString(videoMode: VideoSettingsHelper.VideoMode): String {
        return videoMode.name.toLowerCase(Locale.ENGLISH)
    }

}
