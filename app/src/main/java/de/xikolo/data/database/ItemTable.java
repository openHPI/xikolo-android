package de.xikolo.data.database;

class ItemTable extends Table {

    public static final String TABLE_NAME = "item";

    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_AVAILABLE_FROM = "available_from";
    public static final String COLUMN_AVAILABLE_TO = "available_to";
    public static final String COLUMN_EXERCISE_TYPE = "exercise_type";
    public static final String COLUMN_LOCKED = "locked";

    public static final String COLUMN_VISITED = "visited";
    public static final String COLUMN_COMPLETED = "completed";

    public static final String COLUMN_MODULE_ID = "module_id";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " text primary key, " +
                    COLUMN_POSITION + " integer, " +
                    COLUMN_TITLE + " text, " +
                    COLUMN_TYPE + " text, " +
                    COLUMN_AVAILABLE_FROM + " text, " +
                    COLUMN_AVAILABLE_TO + " text, " +
                    COLUMN_EXERCISE_TYPE + " text, " +
                    COLUMN_LOCKED + " integer, " +
                    COLUMN_VISITED + " integer, " +
                    COLUMN_COMPLETED + " integer, " +
                    COLUMN_MODULE_ID + " text, " +
                    "FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + ModuleTable.TABLE_NAME + "(" + Table.COLUMN_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
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
