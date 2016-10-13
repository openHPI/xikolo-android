package de.xikolo.storages.databases.tables;

public class VideoTable extends Table {

    public static final String TABLE_NAME = "video";

    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_MINUTES = "minutes";
    public static final String COLUMN_SECONDS = "seconds";
    public static final String COLUMN_PROGRESS = "progress";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_DOWNLOAD_URL = "download_url";
    public static final String COLUMN_SLIDES_URL = "slides_url";
    public static final String COLUMN_TRANSCRIPT_URL = "transcript_url";
    public static final String COLUMN_HD_URL = "hd_url";
    public static final String COLUMN_SD_URL = "sd_url";
    public static final String COLUMN_VIMEO_ID = "vimeo_id";
    public static final String COLUMN_POSTER_IMAGE_URL = "poster_image_url";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " text primary key, " +
                    COLUMN_TITLE + " text, " +
                    COLUMN_MINUTES + " text, " +
                    COLUMN_SECONDS + " text, " +
                    COLUMN_PROGRESS + " integer, " +
                    COLUMN_URL + " text, " +
                    COLUMN_DOWNLOAD_URL + " text, " +
                    COLUMN_SLIDES_URL + " text, " +
                    COLUMN_TRANSCRIPT_URL + " text, " +
                    COLUMN_HD_URL + " text, " +
                    COLUMN_SD_URL + " text, " +
                    COLUMN_VIMEO_ID + " text, " +
                    COLUMN_POSTER_IMAGE_URL + " text, " +
                    "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + ItemTable.TABLE_NAME + "(" + Table.COLUMN_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                    ");";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableCreate() {
        return TABLE_CREATE;
    }

}
