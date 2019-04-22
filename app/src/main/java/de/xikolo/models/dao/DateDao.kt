package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import de.xikolo.extensions.asCopy
import de.xikolo.extensions.asLiveData
import de.xikolo.models.CourseDate
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.utils.DateUtil
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

class DateDao(realm: Realm) : BaseDao<CourseDate>(CourseDate::class, realm) {

    init {
        defaultSort = "date" to Sort.ASCENDING
    }

    fun allForCourse(courseId: String): LiveData<List<CourseDate>> =
        query()
            .equalTo("courseId", courseId)
            .sort("date", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

    class Unmanaged {
        companion object {

            fun findNext(): CourseDate? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .greaterThanOrEqualTo("date", Date())
                        .sort("date", Sort.ASCENDING)
                        .findFirst()
                        ?.asCopy()
                }

            fun countToday(): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .between("date", Date(), DateUtil.todaysMidnight())
                        .sort("date", Sort.ASCENDING)
                        .count()
                }

            fun countNextSevenDays(): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .between("date", Date(), DateUtil.nextSevenDays())
                        .sort("date", Sort.ASCENDING)
                        .count()
                }

            fun countFuture(): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .greaterThan("date", Date())
                        .sort("date", Sort.ASCENDING)
                        .count()
                }

            fun countTodayForCourse(courseId: String): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .equalTo("courseId", courseId)
                        .between("date", Date(), DateUtil.todaysMidnight())
                        .sort("date", Sort.ASCENDING)
                        .count()
                }

            fun countNextSevenDaysForCourse(courseId: String): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .equalTo("courseId", courseId)
                        .between("date", Date(), DateUtil.nextSevenDays())
                        .sort("date", Sort.ASCENDING)
                        .count()
                }

            fun countFutureForCourse(courseId: String): Long =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .equalTo("courseId", courseId)
                        .greaterThan("date", Date())
                        .sort("date", Sort.ASCENDING)
                        .count()
                }

            fun allToday(): List<CourseDate> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .between("date", Date(), DateUtil.todaysMidnight())
                        .sort("date", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allNextSevenDaysWithoutToday(): List<CourseDate> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .between("date", DateUtil.todaysMidnight(), DateUtil.nextSevenDays())
                        .sort("date", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allFutureWithoutNextSevenDays(): List<CourseDate> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .greaterThan("date", DateUtil.nextSevenDays())
                        .sort("date", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allTodayForCourse(courseId: String): List<CourseDate> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .equalTo("courseId", courseId)
                        .between("date", Date(), DateUtil.todaysMidnight())
                        .sort("date", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allNextSevenDaysWithoutTodayForCourse(courseId: String): List<CourseDate> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .equalTo("courseId", courseId)
                        .between("date", DateUtil.todaysMidnight(), DateUtil.nextSevenDays())
                        .sort("date", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }

            fun allFutureWithoutNextSevenDaysForCourse(courseId: String): List<CourseDate> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<CourseDate>()
                        .equalTo("courseId", courseId)
                        .greaterThan("date", DateUtil.nextSevenDays())
                        .sort("date", Sort.ASCENDING)
                        .findAll()
                        .asCopy()
                }
        }
    }

}
