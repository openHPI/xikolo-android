package de.xikolo.utils;

import java.io.File;

import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;

public class DownloadUtil {

    public static final String TAG = DownloadUtil.class.getSimpleName();

    public enum VideoAssetType {
        SLIDES, TRANSCRIPT, VIDEO_SD, VIDEO_HD, AUDIO;

        public String getFileSuffix() {
            switch (this) {
                case SLIDES:
                    return "_slides.pdf";
                case TRANSCRIPT:
                    return "_transcript.pdf";
                case VIDEO_SD:
                    return "_video_sd.mp4";
                case VIDEO_HD:
                    return "_video_hd.mp4";
                case AUDIO:
                    return "_audio.mp3";
                default:
                    return "";
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case SLIDES:
                    return "Slides";
                case TRANSCRIPT:
                    return "Transcript";
                case VIDEO_SD:
                    return "SD Video";
                case VIDEO_HD:
                    return "HD Video";
                case AUDIO:
                    return "Audio";
                default:
                    return "";
            }
        }
    }

    public static String getVideoAssetFilePath(VideoAssetType type, Course course, Section section, Item item) {
        return FileUtil.createPublicAppFolderPath() + File.separator
                + FileUtil.escapeFilename(course.title) + "_" + course.id + File.separator
                + FileUtil.escapeFilename(section.title) + "_" + section.id + File.separator
                + FileUtil.escapeFilename(item.title) + type.getFileSuffix();
    }

    public static String getVideoAssetUrl(VideoAssetType type, Video video) {
        switch (type) {
            case VIDEO_HD:
                return video.singleStream.hdUrl;
            case VIDEO_SD:
                return video.singleStream.sdUrl;
            case SLIDES:
                return video.slidesUrl;
            case TRANSCRIPT:
                return video.transcriptUrl;
            case AUDIO:
                return video.audioUrl;
            default:
                return null;
        }
    }

}
