package de.xikolo.lanalytics;

import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import de.xikolo.lanalytics.database.DatabaseHelper;
import de.xikolo.lanalytics.database.Entity;
import de.xikolo.lanalytics.util.ContextUtil;
import de.xikolo.lanalytics.util.DateUtil;

@SuppressWarnings("unused")
public class Lanalytics {

    public static final String TAG = Lanalytics.class.getSimpleName();

    private static Lanalytics instance;

    private Context context;

    private Tracker defaultTracker;

    private DatabaseHelper databaseHelper;

    public static Lanalytics getInstance(Context context) {
        synchronized (Lanalytics.class) {
            if (instance == null) {
                instance = new Lanalytics(context);
            }
        }
        return instance;
    }

    private Lanalytics(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    public Tracker getDefaultTracker() {
        synchronized (Lanalytics.class) {
            if (defaultTracker == null) {
                defaultTracker = new Tracker(context, databaseHelper);
            }
        }
        return defaultTracker;
    }

    public static class Event implements Entity {

        public final String id;

        public final String userId;

        public final String verb;

        public final String resourceId;

        public final Map<String, String> result;

        public final Map<String, String> context;

        public final String timestamp;

        public final boolean onlyWifi;

        private Event(Builder builder) {
            id = builder.id;
            userId = builder.userId;
            verb = builder.verb;
            resourceId = builder.resourceId;
            result = Collections.unmodifiableMap(builder.resultMap);
            context = Collections.unmodifiableMap(builder.contextMap);
            timestamp = builder.timestamp;
            onlyWifi = builder.onlyWifi;
        }

        @Override
        public String getId() {
            return id;
        }

        public static class Builder {

            private transient String id;

            private String userId;

            private String verb;

            private String resourceId;

            private Map<String, String> resultMap;

            private Map<String, String> contextMap;

            private String timestamp;

            private transient boolean onlyWifi;

            public Builder(Cursor cursor) {
                Type typeOfHashMap = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
                Gson gson = new GsonBuilder().create();

                int i = 0;
                id = cursor.getString(i++);
                userId = cursor.getString(i++);
                verb = cursor.getString(i++);
                resourceId = cursor.getString(i++);
                resultMap = gson.fromJson(cursor.getString(i++), typeOfHashMap);
                contextMap = gson.fromJson(cursor.getString(i++), typeOfHashMap);
                timestamp = cursor.getString(i++);
                onlyWifi = cursor.getInt(i) != 0;
            }

            public Builder(Context context) {
                resultMap = new LinkedHashMap<>();
                contextMap = new LinkedHashMap<>();

                id = UUID.randomUUID().toString();

                timestamp = DateUtil.format(new Date());

                contextMap.putAll(ContextUtil.getDefaultContextData(context));

                onlyWifi = false;
            }

            public Builder setUser(String id) {
                this.userId = id;
                return this;
            }

            public Builder setVerb(String verb) {
                this.verb = verb;
                return this;
            }

            public Builder setResource(String id) {
                this.resourceId = id;
                return this;
            }

            public Builder putResult(String key, String value) {
                this.resultMap.put(key, value);
                return this;
            }

            public Builder putAllResults(Map<String, String> results) {
                this.resultMap.putAll(results);
                return this;
            }

            public Builder putContext(String key, String value) {
                this.contextMap.put(key, value);
                return this;
            }

            public Builder putAllContexts(Map<String, String> results) {
                this.contextMap.putAll(results);
                return this;
            }

            public Builder setTimestamp(String timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder setOnlyWifi(boolean onlyWifi) {
                this.onlyWifi = onlyWifi;
                return this;
            }

            public Event build() {
                return new Event(this);
            }

        }

    }

}
