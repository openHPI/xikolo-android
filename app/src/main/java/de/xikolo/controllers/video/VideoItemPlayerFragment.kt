package de.xikolo.controllers.video

import android.content.res.Configuration
import android.os.Bundle
import de.xikolo.controllers.helper.VideoSettingsHelper
import de.xikolo.models.DownloadAsset
import de.xikolo.models.Item
import de.xikolo.models.Video
import de.xikolo.models.VideoSubtitles
import de.xikolo.models.dao.ItemDao
import de.xikolo.models.dao.VideoDao
import de.xikolo.utils.LanalyticsUtil
import io.realm.Realm
import java.util.Locale
import kotlin.math.max

class VideoItemPlayerFragment : VideoStreamPlayerFragment() {

    companion object {
        val TAG: String = VideoItemPlayerFragment::class.java.simpleName

        private const val VIDEO_POSITION_REWIND_TIME = 10000

        private const val BUNDLING_KEY_COURSE_ID = "course_id"
        private const val BUNDLING_KEY_SECTION_ID = "section_id"
        private const val BUNDLING_KEY_ITEM_ID = "item_id"
        private const val BUNDLING_KEY_VIDEO_ID = "video_id"

        fun bundle(instance: VideoItemPlayerFragment, courseId: String, sectionId: String, itemId: String, videoId: String, autoPlay: Boolean = true) {
            bundle(instance, VideoDao.Unmanaged.find(videoId)!!.streamToPlay, autoPlay)

            val arguments = instance.arguments ?: Bundle()
            arguments.putAll(
                Bundle().apply {
                    putString(BUNDLING_KEY_COURSE_ID, courseId)
                    putString(BUNDLING_KEY_SECTION_ID, sectionId)
                    putString(BUNDLING_KEY_ITEM_ID, itemId)
                    putString(BUNDLING_KEY_VIDEO_ID, videoId)
                })

            instance.arguments = arguments
        }

        fun create(courseId: String, sectionId: String, itemId: String, videoId: String, autoPlay: Boolean = true): VideoItemPlayerFragment {
            return VideoItemPlayerFragment().apply {
                bundle(this, courseId, sectionId, itemId, videoId, autoPlay)
            }
        }

        fun unbundle(instance: VideoItemPlayerFragment, arguments: Bundle?) {
            arguments?.let {
                instance.courseId = arguments.getString(BUNDLING_KEY_COURSE_ID)!!
                instance.sectionId = arguments.getString(BUNDLING_KEY_SECTION_ID)!!
                instance.itemId = arguments.getString(BUNDLING_KEY_ITEM_ID)!!
                instance.videoId = arguments.getString(BUNDLING_KEY_VIDEO_ID)!!
            }
        }
    }

    lateinit var courseId: String

    lateinit var sectionId: String

    lateinit var itemId: String

    lateinit var videoId: String

    private val video: Video
        get() = VideoDao.Unmanaged.find(videoId)!!

    private val item: Item
        get() = ItemDao.Unmanaged.find(itemId)!!

    private val videoDownloadAssetHD: DownloadAsset.Course.Item.VideoHD
        get() = DownloadAsset.Course.Item.VideoHD(item, video)

    private val videoDownloadAssetSD: DownloadAsset.Course.Item.VideoSD
        get() = DownloadAsset.Course.Item.VideoSD(item, video)

    private val videoDao = VideoDao(Realm.getDefaultInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unbundle(this, arguments)

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
        return if (downloadAsset.downloadExists) {
            "file://" + downloadAsset.download!!.absolutePath
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
            setLocalVideoUri("file://" + videoAssetDownload.download!!)
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
        return item.downloadExists
    }

    private fun getQualityString(videoMode: VideoSettingsHelper.VideoMode): String {
        return videoMode.name.toLowerCase(Locale.ENGLISH)
    }

}
