package de.xikolo.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "xikolo";

    private SQLiteDatabase db;

    private List<Table> mTables;

    private int mOpenCounter;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mTables = new ArrayList<>();
        mTables.add(new OverallProgressTable());
        mTables.add(new CourseTable());
        mTables.add(new ModuleTable());
        mTables.add(new ItemTable());
        mTables.add(new VideoTable());

        mOpenCounter = 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Table table : mTables) {
            table.onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        for (Table table : mTables) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            db = getWritableDatabase();
        }
        return this.db;
    }

    @Override
    public synchronized void close() {
        if (mOpenCounter > 0) {
            mOpenCounter--;
        }
        if(mOpenCounter == 0) {
            super.close();
        }
    }

    public void deleteDatabase() {
        for (Table table : mTables) {
            table.deleteTable(openDatabase());
        }
    }

}
