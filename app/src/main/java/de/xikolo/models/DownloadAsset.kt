package de.xikolo.models

import de.xikolo.App
import de.xikolo.R
import de.xikolo.services.DownloadService
import de.xikolo.utils.FileUtil
import de.xikolo.utils.StorageUtil
import java.io.File

sealed class DownloadAsset(val url: String?, open val fileName: String, var storage: File = StorageUtil.getStorage(App.getInstance())) {

    // must not end with separator and always have a getter function, otherwise dynamic storage changes will not work
    protected open val fileFolder: String
        get() = storage.absolutePath

    open val title: String
        get() = fileName

    open val size: Long = 0L

    val filePath: String
        get() = fileFolder + File.separator + fileName

    open val mimeType = "application/pdf"

    class Document(
        val document: de.xikolo.models.Document,
        documentLocalization: DocumentLocalization
    ) : DownloadAsset(
        documentLocalization.fileUrl,
        documentLocalization.language + "_" + documentLocalization.revision + "_" + documentLocalization.id + ".pdf"
    ) {
        override val fileFolder
            get() = super.fileFolder + File.separator + "Documents" + File.separator + FileUtil.escapeFilename(document.title) + "_" + document.id

        override val title = "Document (" + documentLocalization.language + "): " + document.title
    }

    sealed class Certificate(url: String?, fileName: String, val course: de.xikolo.models.Course) : DownloadAsset(url, fileName) {
        override val fileFolder
            get() = super.fileFolder + File.separator + "Certificates" + File.separator + FileUtil.escapeFilename(course.title) + "_" + course.id

        class ConfirmationOfParticipation(url: String?, course: de.xikolo.models.Course) : Certificate(url, "confirmation_of_participation.pdf", course) {
            override val title = App.getInstance().getString(R.string.course_confirmation_of_participation) + ": " + course.title
        }

        class RecordOfAchievement(url: String?, course: de.xikolo.models.Course) : Certificate(url, "record_of_achievement.pdf", course) {
            override val title = App.getInstance().getString(R.string.course_record_of_achievement) + ": " + course.title
        }

        class QualifiedCertificate(url: String?, course: de.xikolo.models.Course) : Certificate(url, "qualified_certificate.pdf", course) {
            override val title = App.getInstance().getString(R.string.course_qualified_certificate) + ": " + course.title
        }
    }

    sealed class Course(url: String?, override val fileName: String, val course: de.xikolo.models.Course) : DownloadAsset(url, fileName) {

        override val fileFolder
            get() = super.fileFolder + File.separator + "Courses" + File.separator + FileUtil.escapeFilename(course.title) + "_" + course.id

        sealed class Item(url: String?, fileName: String, val item: de.xikolo.models.Item) : Course(url, fileName, item.section.course) {

            override val fileFolder
                get() = super.fileFolder + File.separator + FileUtil.escapeFilename(item.section.title) + "_" + item.section.id

            override val fileName = FileUtil.escapeFilename(item.title) + "_" + fileName

            class Slides(item: de.xikolo.models.Item, video: Video) : Item(video.slidesUrl, "slides_${item.id}.pdf", item) {
                override val title = "Slides: " + item.title
                override val size = video.slidesSize.toLong()
            }

            class Transcript(item: de.xikolo.models.Item, video: Video) : Item(video.transcriptUrl, "transcript_${item.id}.pdf", item) {
                override val title = "Transcript: " + item.title
                override val size = video.transcriptSize.toLong()
            }

            class VideoSD(item: de.xikolo.models.Item, video: Video) : Item(video.singleStream.sdUrl, "video_sd_${item.id}.mp4", item) {
                override val title = "Video (SD): " + item.title
                override val mimeType = "video/mp4"
                override val size = video.singleStream.sdSize.toLong()
            }

            class VideoHD(item: de.xikolo.models.Item, video: Video) : Item(video.singleStream.hdUrl, "video_hd_${item.id}.mp4", item) {
                override val title = "Video (HD): " + item.title
                override val mimeType = "video/mp4"
                override val size = video.singleStream.hdSize.toLong()
            }

            class Audio(item: de.xikolo.models.Item, video: Video) : Item(video.audioUrl, "audio_${item.id}.mp3", item) {
                override val title = "Audio: " + item.title
                override val mimeType = "audio/mpeg"
                override val size = video.audioSize.toLong()
            }

            class Subtitles(videoSubtitles: VideoSubtitles, item: de.xikolo.models.Item) : Item(videoSubtitles.vttUrl, "subtitles_${videoSubtitles.language}_${item.id}.vtt", item) {
                override val fileFolder
                    get() = super.fileFolder + File.separator + "Subtitles"

                override val title = DownloadService.NO_NOTIFICATION
                override val mimeType = "text/vtt"
            }
        }
    }
}
