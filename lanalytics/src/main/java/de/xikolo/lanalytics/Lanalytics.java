package de.xikolo.lanalytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import de.xikolo.lanalytics.database.DatabaseHelper;
import de.xikolo.lanalytics.database.Entity;
import de.xikolo.lanalytics.parser.Parser;
import de.xikolo.lanalytics.util.ContextUtil;
import de.xikolo.lanalytics.util.DateUtil;
import de.xikolo.lanalytics.util.NetworkUtil;

@SuppressWarnings("unused")
public class Lanalytics {

    public static final String TAG = Lanalytics.class.getSimpleName();

    private static Lanalytics instance;

    private Context context;

    private Tracker defaultTracker;

    private DatabaseHelper databaseHelper;

    private String endpoint;

    public static Lanalytics getInstance(Context context, String endpoint) {
        synchronized (Lanalytics.class) {
            if (instance == null) {
                instance = new Lanalytics(context, endpoint);
            }
        }
        return instance;
    }

    private Lanalytics(Context context, String endpoint) {
        this.context = context;
        this.endpoint = endpoint;
        this.databaseHelper = new DatabaseHelper(context);

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NetworkUtil.isOnline(context)) {
                    getDefaultTracker().startSending();
                }
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public Tracker getDefaultTracker() {
        synchronized (Lanalytics.class) {
            if (defaultTracker == null) {
                defaultTracker = new Tracker(context, endpoint, databaseHelper);
            }
        }
        return defaultTracker;
    }

    public void deleteData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDefaultTracker().stopSending();
                databaseHelper.deleteDatabase();
            }
        }).start();
    }

    public String getDefaultContextDataJson() {
        return Parser.toJson(ContextUtil.getDefaultContextData(context));
    }

    public Map<String, String> getDefaultContextData() {
        return ContextUtil.getDefaultContextData(context);
    }

    public static class Event implements Entity {

        public final String id;

        public final String userId;

        public final String verb;

        public final String resourceId;

        public final String resourceType;

        public final Map<String, String> result;

        public final Map<String, String> context;

        public final String timestamp;

        public final boolean onlyWifi;

        private Event(Builder builder) {
            id = builder.id;
            userId = builder.userId;
            verb = builder.verb;
            resourceId = builder.resourceId;
            resourceType = builder.resourceType;
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

            private String resourceType;

            private Map<String, String> resultMap;

            private Map<String, String> contextMap;

            private String timestamp;

            private transient boolean onlyWifi;

            public Builder(Context context) {
                this();

                id = UUID.randomUUID().toString();

                timestamp = DateUtil.format(new Date());

                contextMap.putAll(ContextUtil.getDefaultContextData(context));

                onlyWifi = false;
            }

            private Builder() {
                resultMap = new LinkedHashMap<>();
                contextMap = new LinkedHashMap<>();
            }

            public static Builder createEmptyBuilder() {
                return new Builder();
            }

            public Builder setId(String id) {
                this.id = id;
                return this;
            }

            public Builder setUser(String id) {
                this.userId = id;
                return this;
            }

            public Builder setVerb(String verb) {
                this.verb = verb;
                return this;
            }

            public Builder setResource(String id, String type) {
                this.resourceId = id;
                this.resourceType = type;
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
