package de.xikolo.models.dao

import androidx.lifecycle.LiveData
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

            fun search(query: String?, withEnrollment: Boolean): List<Course> =
                Realm.getDefaultInstance().use { realm ->
                    val realmQuery = realm.where<Course>()
                        .beginGroup()
                            .like("title", "*$query*", Case.INSENSITIVE)
                            .or()
                            .like("shortAbstract", "*$query*", Case.INSENSITIVE)
                            .or()
                            .like("description", "*$query*", Case.INSENSITIVE)
                            .or()
                            .like("teachers", "*$query*", Case.INSENSITIVE)
                        .endGroup()

                    if (withEnrollment) {
                        realmQuery.isNotNull("enrollmentId")
                    }

                    return realmQuery
                        .sort("startDate", Sort.DESCENDING)
                        .findAll()
                        .asCopy()
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

        }
    }

}
