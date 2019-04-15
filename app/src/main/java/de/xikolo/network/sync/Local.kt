package de.xikolo.network.sync

import android.util.Log
import de.xikolo.config.Config
import io.realm.Realm
import io.realm.RealmModel
import io.realm.kotlin.oneOf

abstract class Local<S : RealmModel> internal constructor(clazz: Class<S>) : Sync<S>(clazz) {

    class Delete<S : RealmModel> private constructor(clazz: Class<S>, private val ids: Array<out String>) : Local<S>(clazz) {

        companion object {

            fun <S : RealmModel> with(clazz: Class<S>, ids: Array<out String>): Delete<S> {
                return Delete(clazz, ids)
            }

            inline fun <reified S : RealmModel> with(ids: Array<out String>): Delete<S> {
                return with(S::class.java, ids)
            }

            inline fun <reified S : RealmModel> with(id: String): Delete<S> {
                return with(S::class.java, arrayOf(id))
            }

        }

        override fun run(): Array<out String> {
            Realm.getDefaultInstance().use { realmInstance ->
                realmInstance.executeTransaction { realm ->
                    val deleteQuery = realm.where(clazz)
                    if (ids.isNotEmpty()) {
                        deleteQuery.oneOf("id", ids)
                    }
                    for (filter in filters) {
                        deleteQuery.equalTo(filter.first, filter.second)
                    }
                    for (filter in inFilters) {
                        deleteQuery.oneOf(filter.first, filter.second)
                    }

                    val results = deleteQuery.findAll()

                    for (result in results) {
                        beforeCommitCallback?.invoke(realm, result)
                    }

                    if (Config.DEBUG) Log.d(Sync.TAG, "DELETE: Deleted ${results.size} local resources from type ${clazz.simpleName}")

                    results.deleteAllFromRealm()
                }
            }

            return ids
        }

    }

    class Update<S : RealmModel> private constructor(clazz: Class<S>, private val ids: Array<out String>) : Local<S>(clazz) {

        companion object {

            fun <S : RealmModel> with(clazz: Class<S>, ids: Array<out String>): Update<S> {
                return Update(clazz, ids)
            }

            inline fun <reified S : RealmModel> with(ids: Array<out String>): Update<S> {
                return with(S::class.java, ids)
            }

            inline fun <reified S : RealmModel> with(id: String): Update<S> {
                return with(S::class.java, arrayOf(id))
            }

        }

        override fun run(): Array<out String> {
            Realm.getDefaultInstance().use { realmInstance ->
                realmInstance.executeTransaction { realm ->
                    val updateQuery = realm.where(clazz)
                    if (ids.isNotEmpty()) {
                        updateQuery.oneOf("id", ids)
                    }
                    for (filter in filters) {
                        updateQuery.equalTo(filter.first, filter.second)
                    }
                    for (filter in inFilters) {
                        updateQuery.oneOf(filter.first, filter.second)
                    }

                    val results = updateQuery.findAll()

                    for (result in results) {
                        beforeCommitCallback?.invoke(realm, result)
                    }

                    realm.copyToRealmOrUpdate<S>(results)

                    if (Config.DEBUG) Log.d(Sync.TAG, "UPDATE: Saved ${results.size} local resources from type ${clazz.simpleName}")
                }
            }

            return ids
        }

    }

}
