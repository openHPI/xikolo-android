package de.xikolo.lanalytics.database.access;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import de.xikolo.lanalytics.Lanalytics;
import de.xikolo.lanalytics.database.DatabaseHelper;
import de.xikolo.lanalytics.database.tables.EventTable;
import de.xikolo.lanalytics.database.tables.Table;

@SuppressWarnings("unused")
public class EventDataAccess extends DataAccess<Lanalytics.Event> {

    public EventDataAccess(DatabaseHelper databaseHelper, Table table) {
        super(databaseHelper, table);
    }

    protected Lanalytics.Event buildEntity(Cursor cursor) {
        return new Lanalytics.Event.Builder(cursor).build();
    }

    protected ContentValues buildContentValues(Lanalytics.Event event) {
        Gson gson = new GsonBuilder().create();

        ContentValues values = new ContentValues();
        values.put(EventTable.COLUMN_ID, event.id);
        values.put(EventTable.COLUMN_USER, event.userId);
        values.put(EventTable.COLUMN_VERB, event.verb);
        values.put(EventTable.COLUMN_RESOURCE, event.resourceId);
        values.put(EventTable.COLUMN_RESULT, gson.toJson(event.result));
        values.put(EventTable.COLUMN_CONTEXT, gson.toJson(event.context));
        values.put(EventTable.COLUMN_TIMESTAMP, event.timestamp);
        values.put(EventTable.COLUMN_WIFI_ONLY, event.onlyWifi);

        return values;
    }

    public List<Lanalytics.Event> getTop(int limit) {
        return getAll("SELECT * FROM " + table.getTableName() + " LIMIT " + limit);
    }

}
