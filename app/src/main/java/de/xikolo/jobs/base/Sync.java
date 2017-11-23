package de.xikolo.jobs.base;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import moe.banana.jsonapi2.Document;
import moe.banana.jsonapi2.Resource;

public abstract class Sync<S extends RealmModel, T extends Resource & RealmAdapter<S>> {

    protected Class<S> clazz;

    protected List<Pair<String, String>> filters;

    protected BeforeCommitCallback<S> beforeCommitCallback;

    protected boolean handleDeletes;

    private Sync(Class<S> clazz) {
        this.clazz = clazz;
        this.filters = new ArrayList<>();
        this.handleDeletes = true;
    }

    public Sync<S, T> addFilters(List<Pair<String, String>> filters) {
        this.filters.addAll(filters);
        return this;
    }

    public Sync<S, T> addFilter(Pair<String, String> filter) {
        this.filters.add(filter);
        return this;
    }

    public Sync<S, T> addFilter(String fieldName, String value) {
        this.filters.add(new Pair<>(fieldName, value));
        return this;
    }

    public Sync<S, T> setBeforeCallback(BeforeCommitCallback<S> callback) {
        this.beforeCommitCallback = callback;
        return this;
    }

    public Sync<S, T> handleDeletes(boolean handleDeletes) {
        this.handleDeletes = handleDeletes;
        return this;
    }

    public interface BeforeCommitCallback<S extends RealmModel>  {
        void beforeCommit(Realm realm, S model);
    }

    public abstract void run();

    public static class Data<S extends RealmModel, T extends Resource & RealmAdapter<S>> extends Sync<S, T> {

        private T[] items;

        private Data(Class<S> clazz, final T[] items) {
            super(clazz);
            this.items = items;
        }

        public static <S extends RealmModel, T extends Resource & RealmAdapter<S>> Data<S, T> with(Class<S> clazz, final T... items) {
            return new Data<>(clazz, items);
        }

        @Override
        public void run() {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    List<String> ids = new ArrayList<>();

                    for (T item : items) {
                        if (beforeCommitCallback != null) {
                            beforeCommitCallback.beforeCommit(realm, item.convertToRealmObject());
                        }
                        realm.copyToRealmOrUpdate(item.convertToRealmObject());
                        ids.add(item.getId());
                    }

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
                        deleteQuery.findAll().deleteAllFromRealm();
                    }
                }
            });
            realm.close();
        }

    }

    public static class Included<S extends RealmModel, T extends Resource & RealmAdapter<S>> extends Sync<S, T> {

        private Document<?> document;

        private Included(Class<S> clazz, Document<?> document) {
            super(clazz);
            this.document = document;
        }

        public static <S extends RealmModel> Included<S, ?> with(Class<S> clazz, final Document<?> document) {
            return new Included<>(clazz, document);
        }

        public static <S extends RealmModel> Included<S, ?> with(Class<S> clazz, final Resource... items) {
            if (items.length > 0) {
                return new Included<>(clazz, items[0].getDocument());
            }
            return new Included<>(clazz, null);
        }

        @Override
        public void run() {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (document == null) return;

                    List<String> ids = new ArrayList<>();

                    for (Resource resource : document.getIncluded()) {
                        if (resource instanceof RealmAdapter) {
                            RealmAdapter adapter = (RealmAdapter) resource;
                            RealmModel model = adapter.convertToRealmObject();
                            if (model.getClass() == clazz) {
                                if (beforeCommitCallback != null) {
                                    beforeCommitCallback.beforeCommit(realm, (S) model);
                                }
                                realm.copyToRealmOrUpdate(model);
                                ids.add(resource.getId());
                            }
                        }
                    }

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
                        deleteQuery.findAll().deleteAllFromRealm();
                    }
                }
            });
            realm.close();
        }

    }

}
