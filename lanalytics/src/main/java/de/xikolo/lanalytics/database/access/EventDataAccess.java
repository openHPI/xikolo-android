package de.xikolo.lanalytics.database.access;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
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

    @SuppressWarnings("unchecked")
    protected Lanalytics.Event buildEntity(Cursor cursor) {
        Type typeOfHashMap = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
        Gson gson = new GsonBuilder().create();

        int i = 0;

        return Lanalytics.Event.Builder.createEmptyBuilder()
                .setId(cursor.getString(i++))
                .setUser(cursor.getString(i++))
                .setVerb(cursor.getString(i++))
                .setResource(cursor.getString(i++), cursor.getString(i++))
                .putAllResults(gson.fromJson(cursor.getString(i++), typeOfHashMap))
                .putAllContexts(gson.fromJson(cursor.getString(i++), typeOfHashMap))
                .setTimestamp(cursor.getString(i++))
                .setOnlyWifi(cursor.getInt(i) != 0)
                .build();
    }

    protected ContentValues buildContentValues(Lanalytics.Event event) {
        Gson gson = new GsonBuilder().create();

        ContentValues values = new ContentValues();
        values.put(EventTable.COLUMN_ID, event.id);
        values.put(EventTable.COLUMN_USER, event.userId);
        values.put(EventTable.COLUMN_VERB, event.verb);
        values.put(EventTable.COLUMN_RESOURCE_ID, event.resourceId);
        values.put(EventTable.COLUMN_RESOURCE_TYPE, event.resourceType);
        values.put(EventTable.COLUMN_RESULT, gson.toJson(event.result));
        values.put(EventTable.COLUMN_CONTEXT, gson.toJson(event.context));
        values.put(EventTable.COLUMN_TIMESTAMP, event.timestamp);
        values.put(EventTable.COLUMN_WIFI_ONLY, event.onlyWifi);

        return values;
    }

    public List<Lanalytics.Event> getTop(int limit) {
        return getAll("SELECT * FROM " + table.getTableName() + " LIMIT " + limit);
    }

    public List<Lanalytics.Event> getTopExcludeWifiOnly(int limit) {
        return getAll("SELECT * FROM " + table.getTableName() + " WHERE " + EventTable.COLUMN_WIFI_ONLY + " = 0 LIMIT " + limit);
    }

    public int getCountExcludeWifiOnly() {
        return getCount("SELECT * FROM " + table.getTableName() + " WHERE " + EventTable.COLUMN_WIFI_ONLY + " = 0");
    }

}
