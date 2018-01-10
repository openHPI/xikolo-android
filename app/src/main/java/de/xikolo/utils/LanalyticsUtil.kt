package de.xikolo.utils

import android.content.res.Configuration
import android.util.Log
import com.google.gson.Gson
import de.xikolo.App
import de.xikolo.config.FeatureToggle
import de.xikolo.lanalytics.Lanalytics
import de.xikolo.managers.UserManager
import de.xikolo.storages.UserStorage

object LanalyticsUtil {

    @JvmField val TAG = LanalyticsUtil::class.simpleName

    @JvmField val CONTEXT_CLIENT_ID = "client_id"

    @JvmField val CONTEXT_COURSE_ID = "course_id"
    @JvmField val CONTEXT_CURRENT_TIME = "current_time"
    @JvmField val CONTEXT_CURRENT_SPEED = "current_speed"
    @JvmField val CONTEXT_OLD_CURRENT_TIME = "old_current_time"
    @JvmField val CONTEXT_NEW_CURRENT_TIME = "new_current_time"
    @JvmField val CONTEXT_OLD_SPEED = "old_speed"
    @JvmField val CONTEXT_NEW_SPEED = "new_speed"
    @JvmField val CONTEXT_HD_VIDEO = "hd_video"

    @JvmField val CONTEXT_SECTION_ID = "section_id"
    @JvmField val CONTEXT_SD_VIDEO = "sd_video"
    @JvmField val CONTEXT_SLIDES = "slides"
    @JvmField val CONTEXT_CURRENT_ORIENTATION = "current_orientation"
    @JvmField val CONTEXT_LANDSCAPE = "landscape"
    @JvmField val CONTEXT_PORTRAIT = "portrait"
    @JvmField val CONTEXT_QUALITY = "current_quality"
    @JvmField val CONTEXT_SOURCE = "current_source"
    @JvmField val CONTEXT_OLD_QUALITY = "old_quality"
    @JvmField val CONTEXT_NEW_QUALITY = "new_quality"
    @JvmField val CONTEXT_OLD_SOURCE = "old_source"
    @JvmField val CONTEXT_NEW_SOURCE = "new_source"
    @JvmField val CONTEXT_CONTENT_TYPE = "content_type"

    @JvmStatic
    val contextDataJson: String
        get() {
            val application = App.getInstance()

            val contextData = application.lanalytics.defaultContextData
            contextData.put(CONTEXT_CLIENT_ID, application.clientId)

            val gson = Gson()
            return gson.toJson(contextData)
        }

    // Video Events

    private fun createVideoEventBuilder(videoId: String, courseId: String, sectionId: String,
                                        currentTime: Int?, currentSpeed: Float?, currentOrientation: Int?, quality: String?, source: String?): Lanalytics.Event.Builder {
        val builder = createEventBuilder()
                .setResource(videoId, "video")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)

        currentTime?.let { builder.putContext(CONTEXT_CURRENT_TIME, formatTime(currentTime)) }
        currentSpeed?.let { builder.putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString()) }
        currentSpeed?.let { builder.putContext(CONTEXT_CURRENT_SPEED, currentSpeed.toString()) }
        currentOrientation?.let {
            builder.putContext(CONTEXT_CURRENT_ORIENTATION, if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) CONTEXT_PORTRAIT else CONTEXT_LANDSCAPE)
        }
        quality?.let { builder.putContext(CONTEXT_QUALITY, quality) }
        source?.let { builder.putContext(CONTEXT_SOURCE, source) }

        return builder
    }

    @JvmStatic
    fun trackVideoPlay(videoId: String, courseId: String, sectionId: String,
                       currentTime: Int, currentSpeed: Float, currentOrientation: Int, quality: String, source: String) {
        createVideoEventBuilder(videoId, courseId, sectionId, currentTime, currentSpeed, currentOrientation, quality, source)
                .setVerb("VIDEO_PLAY").build().track()
    }

    @JvmStatic
    fun trackVideoPause(videoId: String, courseId: String, sectionId: String,
                        currentTime: Int, currentSpeed: Float, currentOrientation: Int, quality: String, source: String) {
        createVideoEventBuilder(videoId, courseId, sectionId, currentTime, currentSpeed, currentOrientation, quality, source)
                .setVerb("VIDEO_PAUSE").build().track()
    }

    @JvmStatic
    fun trackVideoChangeSpeed(videoId: String, courseId: String, sectionId: String,
                              currentTime: Int, oldSpeed: Float?, newSpeed: Float, currentOrientation: Int, quality: String, source: String) {
        createVideoEventBuilder(videoId, courseId, sectionId, currentTime, null, currentOrientation, quality, source)
                .setVerb("VIDEO_CHANGE_SPEED")
                .putContext(CONTEXT_OLD_SPEED, oldSpeed.toString())
                .putContext(CONTEXT_NEW_SPEED, newSpeed.toString())
                .build()
                .track()
    }

    @JvmStatic
    fun trackVideoSeek(videoId: String, courseId: String, sectionId: String,
                       oldCurrentTime: Int, newCurrentTime: Int, currentSpeed: Float, currentOrientation: Int, quality: String, source: String) {
        createVideoEventBuilder(videoId, courseId, sectionId, null, currentSpeed, currentOrientation, quality, source)
                .setVerb("VIDEO_SEEK")
                .putContext(CONTEXT_OLD_CURRENT_TIME, formatTime(oldCurrentTime))
                .putContext(CONTEXT_NEW_CURRENT_TIME, formatTime(newCurrentTime))
                .build()
                .track()
    }

    @JvmStatic
    fun trackVideoChangeOrientation(videoId: String, courseId: String, sectionId: String,
                                    currentTime: Int, currentSpeed: Float, newOrientation: Int, quality: String, source: String) {
        createVideoEventBuilder(videoId, courseId, sectionId, currentTime, currentSpeed, null, quality, source)
                .setVerb(if (newOrientation == Configuration.ORIENTATION_PORTRAIT) "VIDEO_PORTRAIT" else "VIDEO_LANDSCAPE")
                .build()
                .track()
    }

    @JvmStatic
    fun trackVideoChangeQuality(videoId: String, courseId: String, sectionId: String,
                                currentTime: Int, currentSpeed: Float, currentOrientation: Int, oldQuality: String, newQuality: String, oldSource: String, newSource: String) {
        createVideoEventBuilder(videoId, courseId, sectionId, currentTime, currentSpeed, currentOrientation, null, null)
                .setVerb("VIDEO_CHANGE_QUALITY")
                .putContext(CONTEXT_OLD_QUALITY, oldQuality)
                .putContext(CONTEXT_NEW_QUALITY, newQuality)
                .putContext(CONTEXT_OLD_SOURCE, oldSource)
                .putContext(CONTEXT_NEW_SOURCE, newSource)
                .build()
                .track()
    }

    // Download Events

    @JvmStatic
    fun trackDownloadedFile(videoId: String, courseId: String, sectionId: String, type: DownloadUtil.VideoAssetType) {
        val verb: String = when (type) {
            DownloadUtil.VideoAssetType.VIDEO_HD    -> "DOWNLOADED_HD_VIDEO"
            DownloadUtil.VideoAssetType.VIDEO_SD    -> "DOWNLOADED_SD_VIDEO"
            DownloadUtil.VideoAssetType.SLIDES      -> "DOWNLOADED_SLIDES"
            DownloadUtil.VideoAssetType.TRANSCRIPT  -> "DOWNLOADED_TRANSCRIPT"
            DownloadUtil.VideoAssetType.AUDIO       -> "DOWNLOADED_AUDIO"
        }

        createEventBuilder()
                .setResource(videoId, "video")
                .setVerb(verb)
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .build()
                .track()
    }

    @JvmStatic
    fun trackDownloadedSection(sectionId: String, courseId: String, hdVideo: Boolean, sdVideo: Boolean, slides: Boolean) {
        createEventBuilder()
                .setResource(sectionId, "section")
                .setVerb("DOWNLOADED_SECTION")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_HD_VIDEO, hdVideo.toString())
                .putContext(CONTEXT_SD_VIDEO, sdVideo.toString())
                .putContext(CONTEXT_SLIDES, slides.toString())
                .build()
                .track()
    }

    // Visited Events

    @JvmStatic
    fun trackVisitedItem(itemId: String, courseId: String, sectionId: String, contentType: String) {
        createEventBuilder()
                .setResource(itemId, "item")
                .setVerb("VISITED_ITEM")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .putContext(CONTEXT_CONTENT_TYPE, contentType)
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedPinboard(courseId: String) {
        createEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_PINBOARD")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedProgress(courseId: String) {
        createEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_PROGRESS")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedLearningRooms(courseId: String) {
        createEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_LEARNING_ROOMS")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedAnnouncements(courseId: String?) {
        val builder = createEventBuilder()
                .setVerb("VISITED_ANNOUNCEMENTS")
                .setOnlyWifi(true)

        courseId?.let { builder.setResource(courseId, "course") }

        builder.build().track()
    }

    @JvmStatic
    fun trackVisitedAnnouncementDetail(announcementId: String) {
        createEventBuilder()
                .setResource(announcementId, "announcement")
                .setVerb("VISITED_ANNOUNCEMENT_DETAIL")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedRecap(courseId: String) {
        createEventBuilder()
                .setResource(courseId, "course")
                .setVerb("VISITED_RECAP")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedProfile() {
        createEventBuilder()
                .setVerb("VISITED_PROFILE")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedPreferences() {
        createEventBuilder()
                .setVerb("VISITED_PREFERENCES")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    @JvmStatic
    fun trackVisitedDownloads() {
        createEventBuilder()
                .setVerb("VISITED_DOWNLOADS")
                .setOnlyWifi(true)
                .build()
                .track()
    }

    // Second Screen Events

    enum class SecondScreenEvent {
        SLIDES_VISITED, SLIDES_START, SLIDES_STOP,
        TRANSCRIPT_VISITED, TRANSCRIPT_START, TRANSCRIPT_STOP,
        PINBOARD_VISITED, PINBOARD_START, PINBOARD_STOP,
        QUIZ_VISITED, QUIZ_START, QUIZ_STOP
    }

    @JvmStatic
    fun trackSecondScreenEvent(type: SecondScreenEvent, videoId: String, courseId: String, sectionId: String) {
        val builder = createEventBuilder()
                .setResource(videoId, "video")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)

        when (type) {
            SecondScreenEvent.SLIDES_VISITED -> builder.setVerb("VISITED_SECOND_SCREEN_SLIDES")
            SecondScreenEvent.SLIDES_START -> builder.setVerb("SECOND_SCREEN_SLIDES_START")
            SecondScreenEvent.SLIDES_STOP -> builder.setVerb("SECOND_SCREEN_SLIDES_STOP")
            SecondScreenEvent.TRANSCRIPT_VISITED -> builder.setVerb("VISITED_SECOND_SCREEN_TRANSCRIPT")
            SecondScreenEvent.TRANSCRIPT_START -> builder.setVerb("SECOND_SCREEN_TRANSCRIPT_START")
            SecondScreenEvent.TRANSCRIPT_STOP -> builder.setVerb("SECOND_SCREEN_TRANSCRIPT_STOP")
            SecondScreenEvent.PINBOARD_VISITED -> builder.setVerb("VISITED_SECOND_SCREEN_PINBOARD")
            SecondScreenEvent.PINBOARD_START -> builder.setVerb("SECOND_SCREEN_PINBOARD_START")
            SecondScreenEvent.PINBOARD_STOP -> builder.setVerb("SECOND_SCREEN_PINBOARD_STOP")
            SecondScreenEvent.QUIZ_VISITED -> builder.setVerb("VISITED_SECOND_SCREEN_QUIZ")
            SecondScreenEvent.QUIZ_START -> builder.setVerb("SECOND_SCREEN_QUIZ_START")
            SecondScreenEvent.QUIZ_STOP -> builder.setVerb("SECOND_SCREEN_QUIZ_STOP")
        }

        builder.build().track()
    }

    // Text Item Events

    @JvmStatic
    fun trackRichTextFallback(itemId: String, courseId: String, sectionId: String) {
        createEventBuilder()
                .setResource(itemId, "item")
                .setVerb("RICHTEXT_FALLBACK_CLICKED")
                .putContext(CONTEXT_COURSE_ID, courseId)
                .putContext(CONTEXT_SECTION_ID, sectionId)
                .setOnlyWifi(true)
                .build()
                .track()
    }

    // Misc

    fun Lanalytics.Event.track() {
        val application = App.getInstance()
        if (UserManager.isAuthorized && FeatureToggle.tracking()) {
            val tracker = application.lanalytics.defaultTracker
            tracker.send(this, UserManager.token)
            Log.i("Lanalytics", "Created tracking event " + this.verb)
        }
        if (UserManager.isAuthorized && !FeatureToggle.tracking()) Log.d("Lanalytics", "Would have created tracking event " + this.verb)
    }

    private fun createEventBuilder(): Lanalytics.Event.Builder {
        val application = App.getInstance()
        val builder = Lanalytics.Event.Builder(application)

        if (UserManager.isAuthorized) {
            val userStorage = UserStorage()
            builder.setUser(userStorage.userId)
        }
        builder.setResource("00000000-0000-0000-0000-000000000000", "none")
        builder.putContext(CONTEXT_CLIENT_ID, application.clientId)

        return builder
    }

    private fun formatTime(millis: Int): String {
        return (millis / 1000.0).toString()
    }

}
