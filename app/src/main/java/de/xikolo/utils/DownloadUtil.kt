package de.xikolo.utils

import java.io.File

object DownloadUtil {

    val TAG: String = DownloadUtil::class.java.simpleName

    // the file path should identify a download uniquely, thus this AssetDownload object can be used as an identifier for downloads
    sealed class AssetDownload(val url: String?, open val fileName: String) {
        protected open val fileFolder: String = FileUtil.createPublicAppFolderPath() // must not end with separator

        open val title: String
            get() = fileName

        val filePath: String
            get() = fileFolder + File.separator + fileName


        sealed class Course(url: String?, override val fileName: String, val course: de.xikolo.models.Course) : AssetDownload(url, fileName) {
            override val fileFolder = super.fileFolder + File.separator + FileUtil.escapeFilename(course.title) + "_" + FileUtil.escapeFilename(course.id)

            sealed class Item constructor(url: String?, fileName: String, val item: de.xikolo.models.Item, val video: de.xikolo.models.Video) : Course(url, fileName, item.section.course) {
                override val fileFolder = super.fileFolder + File.separator + FileUtil.escapeFilename(item.section.title) + "_" + item.section.id
                override val fileName = FileUtil.escapeFilename(item.title) + "_" + fileName
                abstract val size: Int

                class Slides(item: de.xikolo.models.Item, video: de.xikolo.models.Video) : Item(video.slidesUrl, "slides.pdf", item, video) {
                    override val title = "Slides \"" + item.title + "\""
                    override val size = video.slidesSize
                }

                class Transcript(item: de.xikolo.models.Item, video: de.xikolo.models.Video) : Item(video.transcriptUrl, "transcript.pdf", item, video) {
                    override val title = "Transcript \"" + item.title + "\""
                    override val size = video.transcriptSize
                }

                class VideoSD(item: de.xikolo.models.Item, video: de.xikolo.models.Video) : Item(video.singleStream.sdUrl, "video_sd.mp4", item, video) {
                    override val title = "SD Video \"" + item.title + "\""
                    override val size = video.singleStream.sdSize
                }

                class VideoHD(item: de.xikolo.models.Item, video: de.xikolo.models.Video) : Item(video.singleStream.hdUrl, "video_hd.mp4", item, video) {
                    override val title = "HD Video \"" + item.title + "\""
                    override val size = video.singleStream.hdSize
                }

                class Audio(item: de.xikolo.models.Item, video: de.xikolo.models.Video) : Item(video.audioUrl, "audio.mp3", item, video) {
                    override val title = "Audio \"" + item.title + "\""
                    override val size = video.transcriptSize
                }
            }
        }
    }
}
