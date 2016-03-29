package de.xikolo.data.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

abstract class Table {

    static String COLUMN_ID = BaseColumns._ID;

    abstract String getTableName();

    abstract String getTableCreate();

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getTableCreate());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeTo = oldVersion + 1;

        while (upgradeTo <= newVersion) {

            switch (upgradeTo) {
                case 2:
                    upgradeTo(db, upgradeTo);
                    break;
                default:
                    System.out.println("Deleting " + getTableName());
                    deleteTable(db);
                    break;
            }
            upgradeTo++;

        }
    }

    /**
     * Keep database and migrate tables in subclasses.
     *
     * @param db The SQLite Database
     * @param version The upgrade version
     */
    protected void upgradeTo(SQLiteDatabase db, int version) {}

    public void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + getTableName());
        onCreate(db);
    }

}
