package de.xikolo.utils

import de.xikolo.models.Course
import de.xikolo.models.Item
import de.xikolo.models.Section
import de.xikolo.models.Video
import de.xikolo.utils.DownloadUtil.AbstractItemAsset.Companion.AUDIO
import de.xikolo.utils.DownloadUtil.AbstractItemAsset.Companion.SLIDES
import de.xikolo.utils.DownloadUtil.AbstractItemAsset.Companion.TRANSCRIPT
import de.xikolo.utils.DownloadUtil.AbstractItemAsset.Companion.VIDEO_HD
import de.xikolo.utils.DownloadUtil.AbstractItemAsset.Companion.VIDEO_SD
import java.io.File

object DownloadUtil {

    val TAG = DownloadUtil::class.java.simpleName

    // the file path should identify a download uniquely, thus this AssetDownload object can be used as an identifier downloads
    class AssetDownload(val assetType: AssetType, val url: String?, val filePath: String)

    interface AssetType {
        val type: Int

        val fileSuffix: String

        override fun toString(): String

        interface CourseAssetType : AssetType {
            val course: Course

            interface CertificateAssetType : CourseAssetType

            interface ItemAssetType : CourseAssetType {
                val item: Item
                val section: Section

                interface VideoAssetType : ItemAssetType {
                    val video: Video
                }
            }
        }
    }

    abstract class AbstractItemAsset : AssetType.CourseAssetType.ItemAssetType {

        override val fileSuffix: String
            get() {
                return when (type) {
                    SLIDES      -> "_slides.pdf"
                    TRANSCRIPT  -> "_transcript.pdf"
                    VIDEO_SD    -> "_video_sd.mp4"
                    VIDEO_HD    -> "_video_hd.mp4"
                    AUDIO       -> "_audio.mp3"
                    else        -> ""
                }
            }

        override fun toString(): String {
            return when (type) {
                SLIDES      -> "Slides"
                TRANSCRIPT  -> "Transcript"
                VIDEO_SD    -> "SD Video"
                VIDEO_HD    -> "HD Video"
                AUDIO       -> "Audio"
                else        -> ""
            }
        }

        companion object {
            const val SLIDES = 0
            const val TRANSCRIPT = 1
            const val VIDEO_SD = 2
            const val VIDEO_HD = 3
            const val AUDIO = 4
        }
    }

    abstract class AbstractVideoAsset : AbstractItemAsset(), AssetType.CourseAssetType.ItemAssetType.VideoAssetType

    @JvmStatic
    fun getAssetDownload(type: AssetType, url: String?, fileName: String): AssetDownload {
        var path = FileUtil.createPublicAppFolderPath() + File.separator

        if (type is AssetType.CourseAssetType) {
            path += FileUtil.escapeFilename(type.course.title) + "_" + FileUtil.escapeFilename(type.course.id) + File.separator

            if (type is AssetType.CourseAssetType.CertificateAssetType) {
                path += "certificates" + File.separator
            } else if (type is AssetType.CourseAssetType.ItemAssetType) {
                path += FileUtil.escapeFilename(type.section.title) + "_" + type.section.id + File.separator + FileUtil.escapeFilename(type.item.title)
            }
        }

        path += FileUtil.escapeFilename(fileName) + FileUtil.escapeFilename(type.fileSuffix)

        return AssetDownload(type, url, path)
    }

    @JvmStatic
    fun getVideoAssetUrl(assetType: AssetType.CourseAssetType.ItemAssetType.VideoAssetType): String? {
        return when (assetType.type) {
            VIDEO_HD    -> assetType.video.singleStream.hdUrl
            VIDEO_SD    -> assetType.video.singleStream.sdUrl
            SLIDES      -> assetType.video.slidesUrl
            TRANSCRIPT  -> assetType.video.transcriptUrl
            AUDIO       -> assetType.video.audioUrl
            else        -> null
        }
    }

    @JvmStatic
    fun getVideoAssetType(c: Course, s: Section, i: Item, v: Video, t: Int): DownloadUtil.AssetType.CourseAssetType.ItemAssetType.VideoAssetType {
        return object : DownloadUtil.AbstractVideoAsset() {
            override val item: Item
                get() = i

            override val section: Section
                get() = s

            override val video: Video
                get() = v

            override val course: Course
                get() = c

            override val type: Int
                get() = t
        }
    }

    @JvmStatic
    fun getItemAssetType(c: Course, s: Section, i: Item, t: Int): DownloadUtil.AssetType.CourseAssetType.ItemAssetType {
        return object : DownloadUtil.AbstractItemAsset() {
            override val item: Item
                get() = i
            override val section: Section
                get() = s
            override val course: Course
                get() = c
            override val type: Int
                get() = t
        }
    }

    @JvmStatic
    fun getDefaultItemAssetDownload(type: DownloadUtil.AssetType.CourseAssetType.ItemAssetType): DownloadUtil.AssetDownload {
        return DownloadUtil.getAssetDownload(type, "", "")
    }

    @JvmStatic
    fun getDefaultVideoAssetDownload(type: DownloadUtil.AssetType.CourseAssetType.ItemAssetType.VideoAssetType): DownloadUtil.AssetDownload {
        return DownloadUtil.getAssetDownload(type, getVideoAssetUrl(type), "")
    }

}
