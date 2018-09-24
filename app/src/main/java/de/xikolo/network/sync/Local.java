package de.xikolo.network.sync;

import android.util.Log;
import android.util.Pair;

import de.xikolo.config.Config;
import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import moe.banana.jsonapi2.Resource;

public abstract class Local<S extends RealmModel, T extends Resource & RealmAdapter<S>> extends Sync<S, T> {

    Local(Class<S> clazz) {
        super(clazz);
    }

    public static class Delete<S extends RealmModel, T extends Resource & RealmAdapter<S>> extends Local<S, T> {

        private String[] ids;

        private Delete(Class<S> clazz, final String[] ids) {
            super(clazz);
            this.ids = ids;
        }

        public static <S extends RealmModel> Delete<S, ?> with(Class<S> clazz, final String... ids) {
            return new Delete<>(clazz, ids);
        }

        @Override
        public String[] run() {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction((r) -> {
                RealmQuery<S> deleteQuery = r.where(clazz);
                if (ids.length > 0) {
                    deleteQuery.in("id", ids);
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

                if (beforeCommitCallback != null) {
                    for (S result : results) {
                        beforeCommitCallback.beforeCommit(r, result);
                    }
                }

                if (Config.DEBUG) Log.d(TAG, "DELETE: Deleted " + results.size() + " local resources from type " + clazz.getSimpleName());

                results.deleteAllFromRealm();
            });
            realm.close();

            return ids;
        }

    }

    public static class Update<S extends RealmModel, T extends Resource & RealmAdapter<S>> extends Local<S, T> {

        private String[] ids;

        private Update(Class<S> clazz, final String[] ids) {
            super(clazz);
            this.ids = ids;
        }

        public static <S extends RealmModel> Update<S, ?> with(Class<S> clazz, final String... ids) {
            return new Update<>(clazz, ids);
        }

        @Override
        public String[] run() {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction((r) -> {
                RealmQuery<S> updateQuery = r.where(clazz);
                if (ids.length > 0) {
                    updateQuery.in("id", ids);
                }
                if (filters != null) {
                    for (Pair<String, String> filter : filters) {
                        updateQuery.equalTo(filter.first, filter.second);
                    }
                }
                if (inFilters != null) {
                    for (Pair<String, String[]> filter : inFilters) {
                        updateQuery.in(filter.first, filter.second);
                    }
                }

                RealmResults<S> results = updateQuery.findAll();

                if (beforeCommitCallback != null) {
                    for (S result : results) {
                        beforeCommitCallback.beforeCommit(r, result);
                    }
                }

                r.copyToRealmOrUpdate(results);

                if (Config.DEBUG) Log.d(TAG, "UPDATE: Saved " + results.size() + " local resources from type " + clazz.getSimpleName());
            });
            realm.close();

            return ids;
        }

    }

}
