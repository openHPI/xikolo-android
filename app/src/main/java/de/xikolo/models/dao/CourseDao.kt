package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import de.xikolo.App
import de.xikolo.R
import de.xikolo.extensions.asCopy
import de.xikolo.extensions.asLiveData
import de.xikolo.models.Course
import de.xikolo.models.dao.base.BaseDao
import io.realm.Case
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

class CourseDao(realm: Realm) : BaseDao<Course>(Course::class, realm) {

    init {
        defaultSort = "startDate" to Sort.DESCENDING
    }

    fun all() = all("external" to false)

    fun allSortedByTitle(): LiveData<List<Course>> =
        query()
            .sort("title", Sort.ASCENDING)
            .findAllAsync()
            .asLiveData()

    override fun find(id: String?): LiveData<Course> =
        query()
            .beginGroup()
                .equalTo("id", id)
                .or()
                .equalTo("slug", id)
            .endGroup()
            .findFirstAsync()
            .asLiveData()

    class Unmanaged {
        companion object {

            @JvmStatic
            fun find(id: String?): Course? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .beginGroup()
                            .equalTo("id", id)
                            .or()
                            .equalTo("slug", id)
                        .endGroup()
                        .findFirst()
                        ?.asCopy()
                }

            fun allWithCertificates(): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
                        .filter { course ->
                            EnrollmentDao.Unmanaged.findForCourse(course.id)
                                ?.anyCertificateAchieved() == true
                        }
                }

            fun filter(searchQuery: String?, filterMap: Map<String, String>, withEnrollment: Boolean): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    val realmQuery = realm.where<Course>()
                        .beginGroup()
                            .like("title", "*$searchQuery*", Case.INSENSITIVE)
                            .or()
                            .like("shortAbstract", "*$searchQuery*", Case.INSENSITIVE)
                            .or()
                            .like("description", "*$searchQuery*", Case.INSENSITIVE)
                            .or()
                            .like("teachers", "*$searchQuery*", Case.INSENSITIVE)
                        .endGroup()

                    filterMap.forEach {
                        if (it.value != App.instance.getString(R.string.course_filter_classifier_all)) {
                            when (it.key) {
                                App.instance.getString(R.string.course_filter_classifier_channel)  -> {
                                    realmQuery.equalTo("channelId", it.value, Case.INSENSITIVE)
                                }
                                App.instance.getString(R.string.course_filter_classifier_language) -> {
                                    realmQuery.equalTo("language", it.value, Case.INSENSITIVE)
                                }
                            }
                        }
                    }

                    if (withEnrollment) {
                        realmQuery.isNotNull("enrollmentId")
                    }

                    return realmQuery
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
                        .filter { course ->
                            filterMap.all {
                                it.value == App.instance.getString(R.string.course_filter_classifier_all)
                                    || it.key == App.instance.getString(R.string.course_filter_classifier_channel)
                                    || it.key == App.instance.getString(R.string.course_filter_classifier_language)
                                    || Gson().fromJson<Map<String, List<String>>>(course.classifiers, Map::class.java)[it.key]?.contains(it.value) == true
                            }
                        }
                }

            fun allCurrentAndFuture(): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .beginGroup()
                            .isNull("endDate")
                            .or()
                            .greaterThanOrEqualTo("endDate", Date())
                        .endGroup()
                        .sort("startDate", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allCurrentAndPast(): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .lessThanOrEqualTo("startDate", Date())
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allPast(): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .beginGroup()
                            .isNotNull("endDate")
                            .and()
                            .lessThan("endDate", Date())
                        .endGroup()
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allFuture(): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .greaterThan("startDate", Date())
                        .sort("startDate", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allCurrentAndPastWithEnrollment(): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .equalTo("external", false)
                        .isNotNull("enrollmentId")
                        .lessThanOrEqualTo("startDate", Date())
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allFutureWithEnrollment(): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .equalTo("external", false)
                        .isNotNull("enrollmentId")
                        .greaterThan("startDate", Date())
                        .sort("startDate", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allCurrentAndFutureForChannel(channelId: String?): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .equalTo("channelId", channelId)
                        .equalTo("external", false)
                        .beginGroup()
                            .isNull("endDate")
                            .or()
                            .greaterThanOrEqualTo("endDate", Date())
                        .endGroup()
                        .sort("startDate", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allCurrentAndPastForChannel(channelId: String?): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .equalTo("channelId", channelId)
                        .equalTo("external", false)
                        .lessThanOrEqualTo("startDate", Date())
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allPastForChannel(channelId: String?): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .equalTo("channelId", channelId)
                        .equalTo("external", false)
                        .beginGroup()
                            .isNotNull("endDate")
                            .and()
                            .lessThan("endDate", Date())
                        .endGroup()
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allFutureForChannel(channelId: String?): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .equalTo("channelId", channelId)
                        .equalTo("external", false)
                        .greaterThan("startDate", Date())
                        .sort("startDate", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun languages(): List<String> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Course>()
                        .findAll()
                        .asCopy()
                    }
                    .map {
                        it.language
                    }
                    .distinct()

            fun collectClassifier(name: String): List<String> =
                Realm.getDefaultInstance()
                    .use { realm ->
                        realm.where<Course>()
                            .findAll()
                            .asCopy()
                    }
                    .map {
                        val map = Gson().fromJson<Map<String, List<String>>>(
                            it.classifiers, Map::class.java
                        )
                        map?.get(name) ?: listOf()
                    }
                    .fold(mutableListOf<String>(), { acc, list ->
                        acc.apply { addAll(list) }
                    })
                    .distinct()
        }
    }

}
