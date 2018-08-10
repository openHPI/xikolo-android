package de.xikolo.models.base;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.config.Config;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import moe.banana.jsonapi2.Document;
import moe.banana.jsonapi2.Resource;

public abstract class Sync<S extends RealmModel, T extends Resource & RealmAdapter<S>> {

    public static final String TAG = Sync.class.getSimpleName();

    Class<S> clazz;

    List<Pair<String, String>> filters;

    List<Pair<String, String[]>> inFilters;

    BeforeCommitCallback<S> beforeCommitCallback;

    boolean handleDeletes;

    Sync(Class<S> clazz) {
        this.clazz = clazz;
        this.filters = new ArrayList<>();
        this.inFilters = new ArrayList<>();
        this.handleDeletes = true;
    }

    public Sync<S, T> addFilter(String fieldName, String value) {
        this.filters.add(new Pair<>(fieldName, value));
        return this;
    }

    public Sync<S, T> addFilter(String fieldName, String[] values) {
        this.inFilters.add(new Pair<>(fieldName, values));
        return this;
    }

    public Sync<S, T> setBeforeCommitCallback(BeforeCommitCallback<S> callback) {
        this.beforeCommitCallback = callback;
        return this;
    }

    /**
     * Sync resources without deleting untouched resources
     */
    public Sync<S, T> saveOnly() {
        this.handleDeletes = false;
        return this;
    }

    public interface BeforeCommitCallback<S extends RealmModel>  {
        void beforeCommit(Realm realm, S model);
    }

    public abstract String[] run();

    public static class Data<S extends RealmModel, T extends Resource & RealmAdapter<S>> extends Sync<S, T> {

        private T[] items;

        private Data(Class<S> clazz, final T[] items) {
            super(clazz);
            this.items = items;
        }

        @SafeVarargs
        public static <S extends RealmModel, T extends Resource & RealmAdapter<S>> Data<S, T> with(Class<S> clazz, final T... items) {
            return new Data<>(clazz, items);
        }

        @Override
        public String[] run() {
            final List<String> ids = new ArrayList<>();

            try (Realm realmInstance = Realm.getDefaultInstance()) {
                realmInstance.executeTransaction(realm -> {
                    for (T item : items) {
                        S model = item.convertToRealmObject();
                        if (beforeCommitCallback != null) {
                            beforeCommitCallback.beforeCommit(realm, model);
                        }
                        realm.copyToRealmOrUpdate(model);
                        ids.add(item.getId());
                    }

                    if (Config.DEBUG) Log.d(TAG, "DATA: Saved " + items.length + " data resources from type " + clazz.getSimpleName());

                    if (handleDeletes) {
                        RealmQuery<S> deleteQuery = realm.where(clazz);
                        if (ids.size() > 0) {
                            deleteQuery.not().in("id", ids.toArray(new String[0]));
                        }
                        if (filters != null) {
                            for (Pair<String, String> filter : filters) {
                                deleteQuery.equalTo(filter.first, filter.second);
                            }
                        }
                        if (inFilters != null) {
                            for (Pair<String, String[]> filter : inFilters) {
                                deleteQuery.in(filter.first, filter.second);
                            }
                        }
                        RealmResults<S> results = deleteQuery.findAll();

                        if (Config.DEBUG) Log.d(TAG, "DATA: Deleted " + results.size() + " local resources from type " + clazz.getSimpleName());

                        results.deleteAllFromRealm();
                    } else if (Config.DEBUG) Log.d(TAG, "DATA: Deleted 0 local resources from type " + clazz.getSimpleName());
                });
            }

            return ids.toArray(new String[0]);
        }

    }

    public static class Included<S extends RealmModel, T extends Resource & RealmAdapter<S>> extends Sync<S, T> {

        private Document document;

        private Included(Class<S> clazz, Document document) {
            super(clazz);
            this.document = document;
        }

        public static <S extends RealmModel> Included<S, ?> with(Class<S> clazz, final Document document) {
            return new Included<>(clazz, document);
        }

        public static <S extends RealmModel> Included<S, ?> with(Class<S> clazz, final Resource... items) {
            if (items.length > 0) {
                return new Included<>(clazz, items[0].getDocument());
            }
            return new Included<>(clazz, null);
        }

        @Override
        public String[] run() {
            if (document == null) return new String[0];

            final List<String> ids = new ArrayList<>();

            try (Realm realmInstance = Realm.getDefaultInstance()) {
                realmInstance.executeTransaction(realm -> {
                    for (Resource resource : document.getIncluded()) {
                        if (resource instanceof RealmAdapter) {
                            RealmAdapter adapter = (RealmAdapter) resource;
                            RealmModel model = adapter.convertToRealmObject();
                            if (model.getClass() == clazz) {
                                if (beforeCommitCallback != null) {
                                    //noinspection unchecked
                                    beforeCommitCallback.beforeCommit(realm, (S) model);
                                }
                                realm.copyToRealmOrUpdate(model);
                                ids.add(resource.getId());
                            }
                        }
                    }

                    if (Config.DEBUG) Log.d(TAG, "INCLUDED: Saved " + ids.size() + " included resources from type " + clazz.getSimpleName());

                    if (handleDeletes) {
                        RealmQuery<S> deleteQuery = realm.where(clazz);
                        if (ids.size() > 0) {
                            deleteQuery.not().in("id", ids.toArray(new String[0]));
                        }
                        if (filters != null) {
                            for (Pair<String, String> filter : filters) {
                                deleteQuery.equalTo(filter.first, filter.second);
                            }
                        }
                        if (inFilters != null) {
                            for (Pair<String, String[]> filter : inFilters) {
                                deleteQuery.in(filter.first, filter.second);
                            }
                        }
                        RealmResults<S> results = deleteQuery.findAll();

                        if (Config.DEBUG) Log.d(TAG, "INCLUDED: Deleted " + results.size() + " local resources from type " + clazz.getSimpleName());

                        results.deleteAllFromRealm();
                    } else if (Config.DEBUG) Log.d(TAG, "INCLUDED: Deleted 0 local resources from type " + clazz.getSimpleName());
                });
            }

            return ids.toArray(new String[0]);
        }

    }

}
