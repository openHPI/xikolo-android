package de.xikolo.data.database;

class ModuleTable extends Table {

    public static final String TABLE_NAME = "module";

    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AVAILABLE_FROM = "available_from";
    public static final String COLUMN_AVAILABLE_TO = "available_to";
    public static final String COLUMN_LOCKED = "locked";

    public static final String COLUMN_COURSE_ID = "course_id";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " text primary key, " +
                    COLUMN_POSITION + " integer, " +
                    COLUMN_NAME + " text, " +
                    COLUMN_AVAILABLE_FROM + " text, " +
                    COLUMN_AVAILABLE_TO + " text, " +
                    COLUMN_LOCKED + " integer, " +
                    COLUMN_COURSE_ID + " text, " +
                    "FOREIGN KEY(" + COLUMN_COURSE_ID + ") REFERENCES " + CourseTable.TABLE_NAME + "(" + Table.COLUMN_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
                    ");";

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    String getTableCreate() {
        return TABLE_CREATE;
    }

}
