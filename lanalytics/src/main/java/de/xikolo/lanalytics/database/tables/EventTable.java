package de.xikolo.lanalytics.database.tables;

public class EventTable extends Table {

    public static final String TABLE_NAME = "event";

    public static final String COLUMN_USER = "user";
    public static final String COLUMN_VERB = "verb";
    public static final String COLUMN_RESOURCE = "resource";
    public static final String COLUMN_RESULT = "result";
    public static final String COLUMN_CONTEXT = "context";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_WIFI_ONLY = "wifi_only";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " text primary key, " +
                    COLUMN_USER + " text, " +
                    COLUMN_VERB + " text, " +
                    COLUMN_RESOURCE + " text, " +
                    COLUMN_RESULT + " text, " +
                    COLUMN_CONTEXT + " text, " +
                    COLUMN_TIMESTAMP + " text, " +
                    COLUMN_WIFI_ONLY + " integer " +
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
