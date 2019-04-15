package de.xikolo.network.sync

import android.util.Log
import de.xikolo.config.Config
import de.xikolo.models.base.RealmAdapter
import io.realm.Realm
import io.realm.RealmModel
import io.realm.kotlin.oneOf
import moe.banana.jsonapi2.Document
import moe.banana.jsonapi2.Resource
import java.util.*

abstract class Sync<S : RealmModel> internal constructor(internal var clazz: Class<S>) {

    companion object {
        val TAG: String = Sync::class.java.simpleName
    }

    internal var filters: MutableList<Pair<String, String>> = ArrayList()

    internal var inFilters: MutableList<Pair<String, Array<out String>>> = ArrayList()

    internal var beforeCommitCallback: ((realm: Realm, model: S) -> Unit)? = null

    internal var handleDeletes: Boolean = true

    fun addFilter(fieldName: String, value: String): Sync<S> {
        this.filters.add(Pair(fieldName, value))
        return this
    }

    fun addFilter(fieldName: String, values: Array<out String>): Sync<S> {
        this.inFilters.add(Pair(fieldName, values))
        return this
    }

    fun setBeforeCommitCallback(callback: (realm: Realm, model: S) -> Unit): Sync<S> {
        this.beforeCommitCallback = callback
        return this
    }

    /**
     * Sync resources without deleting untouched resources
     */
    fun saveOnly(): Sync<S> {
        this.handleDeletes = false
        return this
    }

    abstract fun run(): Array<out String>

    class Data<S : RealmModel, T> private constructor(clazz: Class<S>, private val items: Array<T>) : Sync<S>(clazz)
            where T : Resource,
                  T : RealmAdapter<S> {

        companion object {

            fun <S : RealmModel, T> with(clazz: Class<S>, items: Array<T>): Data<S, T>
                    where T : Resource,
                          T : RealmAdapter<S> {
                return Data(clazz, items)
            }

            inline fun <reified S : RealmModel, reified T> with(items: Array<T>): Data<S, T>
                    where T : Resource,
                          T : RealmAdapter<S> {
                return with(S::class.java, items)
            }

            inline fun <reified S : RealmModel, reified T> with(item: T): Data<S, T>
                    where T : Resource,
                          T : RealmAdapter<S> {
                return with(S::class.java, arrayOf(item))
            }

        }

        override fun run(): Array<out String> {
            val ids = ArrayList<String>()

            Realm.getDefaultInstance().use { realmInstance ->
                realmInstance.executeTransaction { realm ->
                    for (item in items) {
                        val model = item.convertToRealmObject()
                        beforeCommitCallback?.invoke(realm, model)
                        realm.copyToRealmOrUpdate(model)
                        ids.add(item.id)
                    }

                    if (Config.DEBUG) Log.d(TAG, "DATA: Saved ${items.size} data resources from type ${clazz.simpleName}")

                    if (handleDeletes) {
                        val deleteQuery = realm.where(clazz)
                        if (ids.size > 0) {
                            deleteQuery.not().oneOf("id", ids.toTypedArray())
                        }
                        for (filter in filters) {
                            deleteQuery.equalTo(filter.first, filter.second)
                        }
                        for (filter in inFilters) {
                            deleteQuery.oneOf(filter.first, filter.second)
                        }
                        val results = deleteQuery.findAll()

                        if (Config.DEBUG) Log.d(TAG, "DATA: Deleted ${results.size} local resources from type ${clazz.simpleName}")

                        results.deleteAllFromRealm()
                    } else if (Config.DEBUG) Log.d(TAG, "DATA: Deleted 0 local resources from type ${clazz.simpleName}")
                }
            }

            return ids.toTypedArray()
        }

    }

    class Included<S : RealmModel> private constructor(clazz: Class<S>, private val document: Document?) : Sync<S>(clazz) {

        companion object {

            fun <S : RealmModel> with(clazz: Class<S>, items: Array<out Resource>): Included<S> {
                return if (items.isNotEmpty()) {
                    Included(clazz, items.first().document)
                } else {
                    Included(clazz, null)
                }
            }

            inline fun <reified S : RealmModel> with(items: Array<out Resource>): Included<S> {
                return with(S::class.java, items)
            }

            inline fun <reified S : RealmModel> with(item: Resource): Included<S> {
                return with(S::class.java, arrayOf(item))
            }

        }

        override fun run(): Array<out String> {
            if (document == null) return arrayOf()

            val ids = ArrayList<String>()

            Realm.getDefaultInstance().use { realmInstance ->
                realmInstance.executeTransaction { realm ->
                    for (resource in document.included) {
                        if (resource is RealmAdapter<*>) {
                            val adapter = resource as RealmAdapter<*>
                            val model = adapter.convertToRealmObject()
                            if (model.javaClass == clazz) {
                                @Suppress("UNCHECKED_CAST")
                                beforeCommitCallback?.invoke(realm, model as S)
                                realm.copyToRealmOrUpdate(model)
                                ids.add(resource.id)
                            }
                        }
                    }

                    if (Config.DEBUG) Log.d(TAG, "INCLUDED: Saved ${ids.size} included resources from type ${clazz.simpleName}")

                    if (handleDeletes) {
                        val deleteQuery = realm.where(clazz)
                        if (ids.size > 0) {
                            deleteQuery.not().oneOf("id", ids.toTypedArray())
                        }
                        for (filter in filters) {
                            deleteQuery.equalTo(filter.first, filter.second)
                        }
                        for (filter in inFilters) {
                            deleteQuery.oneOf(filter.first, filter.second)
                        }
                        val results = deleteQuery.findAll()

                        if (Config.DEBUG) Log.d(TAG, "INCLUDED: Deleted ${results.size} local resources from type ${clazz.simpleName}")

                        results.deleteAllFromRealm()
                    } else if (Config.DEBUG) Log.d(TAG, "INCLUDED: Deleted 0 local resources from type ${clazz.simpleName}")
                }
            }

            return ids.toTypedArray()
        }

    }

}
