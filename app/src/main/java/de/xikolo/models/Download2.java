package de.xikolo.models;

public class Download2 {

    public int id;

    public String url;

    public String filePath;

    public long totalBytes;

    public long bytesWritten;

    public State state = State.PENDING;

    public enum State {
        PENDING,
        RUNNING,
        SUCCESSFUL,
        FAILURE,
    }

    public enum FileType {
        SLIDES, TRANSCRIPT, VIDEO_SD, VIDEO_HD;

        public static FileType getFileType(String filePath) {
            if (filePath.endsWith(SLIDES.getFileSuffix())) {
                return SLIDES;
            }
            if (filePath.endsWith(TRANSCRIPT.getFileSuffix())) {
                return TRANSCRIPT;
            }
            if (filePath.endsWith(VIDEO_SD.getFileSuffix())) {
                return VIDEO_SD;
            }
            if (filePath.endsWith(VIDEO_HD.getFileSuffix())) {
                return VIDEO_HD;
            }
            return null;
        }

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
                default:
                    return "";
            }
        }

    }
    
}
