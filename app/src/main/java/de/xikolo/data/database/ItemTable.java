package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;

class ItemTable implements Table {

    public static final String TABLE_NAME = "item";

    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_AVAILABLE_FROM = "available_from";
    public static final String COLUMN_AVAILABLE_TO = "available_to";
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
                    COLUMN_LOCKED + " integer, " +
                    COLUMN_VISITED + " integer, " +
                    COLUMN_COMPLETED + " integer, " +
                    COLUMN_MODULE_ID + " text, " +
                    "FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + ModuleTable.TABLE_NAME + "(" + Table.COLUMN_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
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
                    // keep db
                    System.out.println("Keeping " + TABLE_NAME);
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
