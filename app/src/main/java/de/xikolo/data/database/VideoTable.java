package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;

class VideoTable implements Table {

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
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1) {

            switch(newVersion) {
                case 2:
                    System.out.println("Adding Column " + COLUMN_PROGRESS);
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_PROGRESS + " INTEGER DEFAULT 0");
                    break;
                default:
                    System.out.println("Deleting " + TABLE_NAME);
                    deleteTable(db);
                    break;
            }

        } else {
            System.out.println("Deleting " + TABLE_NAME);
            deleteTable(db);
        }
    }

    @Override
    public void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
