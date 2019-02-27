package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Case
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

class CoursesDao(realm: Realm) : BaseDao(realm) {

    fun enrollments(): LiveData<List<Enrollment>> =
        realm
            .where<Enrollment>()
            .findAllAsync()
            .asLiveData()

    fun enrollmentCount(): Long =
        realm
            .where<Enrollment>()
            .count()

    fun coursesWithCertificates(): List<Course> = realm.copyFromRealm(
        realm
            .where<Course>()
            .equalTo("external", false)
            .sort("startDate", Sort.DESCENDING)
            .findAll())
        .filter {
            val enrollment = Enrollment.getForCourse(it.id)
            enrollment != null && enrollment.anyCertificateAchieved()
        }

    fun courses(): LiveData<List<Course>> =
        realm
            .where<Course>()
            .equalTo("external", false)
            .sort("startDate", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

    fun enrolledCourses(): LiveData<List<Course>> =
        realm
            .where<Course>()
            .equalTo("external", false)
            .isNotNull("enrollmentId")
            .sort("startDate", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

    fun searchCourses(query: String, withEnrollment: Boolean): List<Course> {
        var dbQuery = realm
            .where<Course>()
            .equalTo("external", false)
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
            dbQuery = dbQuery.isNotNull("enrollmentId")
        }

        return dbQuery
            .sort("startDate", Sort.DESCENDING)
            .findAll()
    }

    fun course(id: String): LiveData<Course> =
        realm
            .where<Course>()
            .beginGroup()
                .equalTo("id", id)
                .or()
                .equalTo("slug", id)
            .endGroup()
            .findFirstAsync()
            .asLiveData()

    fun currentAndFutureCourses(): List<Course> =
        realm
            .where(Course::class.java)
            .equalTo("external", false)
            .greaterThanOrEqualTo("endDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun currentAndPastCourses(): List<Course> =
        realm
            .where<Course>()
            .equalTo("external", false)
            .lessThanOrEqualTo("startDate", Date())
            .sort("startDate", Sort.DESCENDING)
            .findAll()

    fun pastCourses(): List<Course> =
        realm
            .where<Course>()
            .equalTo("external", false)
            .lessThan("endDate", Date())
            .sort("startDate", Sort.DESCENDING)
            .findAll()

    fun futureCourses(): List<Course> =
        realm
            .where<Course>()
            .equalTo("external", false)
            .greaterThan("startDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun currentAndPastCoursesWithEnrollment(): List<Course> =
        realm
            .where<Course>()
            .equalTo("external", false)
            .isNotNull("enrollmentId")
            .lessThanOrEqualTo("startDate", Date())
            .sort("startDate", Sort.DESCENDING)
            .findAll()

    fun futureCoursesWithEnrollment(): List<Course> =
        realm
            .where<Course>()
            .equalTo("external", false)
            .isNotNull("enrollmentId")
            .greaterThan("startDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun coursesForChannel(channelId: String): LiveData<List<Course>> =
        realm
            .where(Course::class.java)
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .sort("startDate", Sort.DESCENDING)
            .findAllAsync()
            .asLiveData()

    fun currentAndFutureCoursesForChannel(channelId: String): List<Course> =
        realm
            .where<Course>()
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .greaterThanOrEqualTo("endDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun currentAndPastCoursesForChannel(channelId: String): List<Course> =
        realm
            .where<Course>()
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .lessThanOrEqualTo("startDate", Date())
            .sort("startDate", Sort.DESCENDING)
            .findAll()

    fun pastCoursesForChannel(channelId: String): List<Course> =
        realm
            .where<Course>()
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .lessThan("endDate", Date())
            .sort("startDate", Sort.DESCENDING)
            .findAll()

    fun futureCoursesForChannel(channelId: String): List<Course> =
        realm
            .where<Course>()
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .greaterThan("startDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

}
