package de.xikolo.models.dao.base

import androidx.lifecycle.LiveData
import de.xikolo.extensions.asLiveData
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.Sort
import kotlin.reflect.KClass

/**
 * The base class for data access objects.
 *
 * Inherit this class to implement a data access object for a certain model class.
 * Subclasses should be the only ones in the project who implement Realm-specific code,
 * like database queries. We agreed on some conventions:
 * - single entity request methods begin with `find`, collection requests begin with `all`
 * - managed requests with results wrapped as `LiveData` are implemented as instance methods
 * - unmanaged requests with plain object/list results are implemented in an `Unmanaged` object
 *
 * @param T the type of the model this class belongs to.
 * @property clazz the model's class.
 * @realm realm the managed Realm instance for managed requests.
 */
open class BaseDao<T : RealmObject>(private val clazz: KClass<T>, val realm: Realm) {

    var defaultSort: Pair<String, Sort>? = null

    fun query(): RealmQuery<T> = realm.where(clazz.java)

    open val unmanaged = object { }

    open fun find(id: String?): LiveData<T> =
        query()
            .equalTo("id", id)
            .findFirstAsync()
            .asLiveData()

    fun all(vararg equalToClauses: Pair<String, Any?>): LiveData<List<T>> {
        val query = query()

        for (equalTo in equalToClauses) {
            val value = equalTo.second
            when (value) {
                is String  -> query.equalTo(equalTo.first, value)
                is Boolean -> query.equalTo(equalTo.first, value)
            }
        }

        defaultSort?.let {
            query.sort(it.first, it.second)
        }

        return query.findAllAsync().asLiveData()
    }

}
