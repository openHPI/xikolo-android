package de.xikolo.lanalytics;

import android.content.Context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.xikolo.lanalytics.util.ContextUtil;

public class Lanalytics {

    private static Lanalytics instance;

    private Context context;

    private Tracker defaultTracker;

    public Lanalytics getInstance(Context context) {
        synchronized (Lanalytics.class) {
            if (instance == null) {
                instance = new Lanalytics(context);
            }
        }
        return instance;
    }

    private Lanalytics(Context context) {
        this.context = context;
    }

    public Tracker getDefaultTracker() {
        synchronized (Lanalytics.class) {
            if (defaultTracker == null) {
                defaultTracker = new Tracker();
            }
        }
        return defaultTracker;
    }

    public static class Event {

        private final String userId;

        private final String verb;

        private final String resourceId;

        private final Map<String, String> result;

        private final Map<String, String> context;

        private final String timestamp;

        private final boolean onlyWifi;

        private Event(Builder builder) {
            userId = builder.userId;
            verb = builder.verb;
            resourceId = builder.resourceId;
            result = Collections.unmodifiableMap(builder.resultMap);
            context = Collections.unmodifiableMap(builder.contextMap);
            timestamp = builder.timestamp;
            onlyWifi = builder.onlyWifi;
        }

        public String getUser() {
            return userId;
        }

        public String getVerb() {
            return verb;
        }

        public String getResource() {
            return resourceId;
        }

        public Map<String, String> getResult() {
            return result;
        }

        public Map<String, String> getContext() {
            return context;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public static class Builder {

            private String userId;

            private String verb;

            private String resourceId;

            private Map<String, String> resultMap;

            private Map<String, String> contextMap;

            private String timestamp;

            private boolean onlyWifi;

            public Builder(Context context) {
                resultMap = new LinkedHashMap<>();
                contextMap = new LinkedHashMap<>();

                // TODO set timestamp
                timestamp = "";

                contextMap.putAll(ContextUtil.getDefaultContextData(context));
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
