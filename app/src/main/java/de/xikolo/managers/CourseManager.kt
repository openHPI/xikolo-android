package de.xikolo.managers

import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class CourseManager {

    companion object {
        val TAG: String = CourseManager::class.java.simpleName
    }

    fun listEnrollments(realm: Realm, listener: RealmChangeListener<RealmResults<Enrollment>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val enrollmentListPromise = realm
            .where(Enrollment::class.java)
            .findAllAsync()

        enrollmentListPromise.addChangeListener(listener)

        return enrollmentListPromise
    }

    fun getCourse(id: String, realm: Realm, listener: RealmChangeListener<Course>?): Course {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val coursePromise = realm
            .where(Course::class.java)
            .beginGroup()
                .equalTo("id", id)
                .or()
                .equalTo("slug", id)
            .endGroup()
            .findFirstAsync()

        coursePromise.addChangeListener(listener)

        return coursePromise
    }

    fun listCurrentAndFutureCoursesForChannel(realm: Realm, channelId: String): List<Course> = realm
        .where(Course::class.java)
        .equalTo("channelId", channelId)
        .equalTo("external", false)
        .greaterThanOrEqualTo("endDate", Date())
        .sort("startDate", Sort.ASCENDING)
        .findAll()

    fun listCurrentAndPastCoursesForChannel(realm: Realm, channelId: String): List<Course> = realm
        .where(Course::class.java)
        .equalTo("channelId", channelId)
        .equalTo("external", false)
        .lessThanOrEqualTo("startDate", Date())
        .sort("startDate", Sort.DESCENDING)
        .findAll()

    fun listPastCoursesForChannel(realm: Realm, channelId: String): List<Course> = realm
        .where(Course::class.java)
        .equalTo("channelId", channelId)
        .equalTo("external", false)
        .lessThan("endDate", Date())
        .sort("startDate", Sort.DESCENDING)
        .findAll()

    fun listFutureCoursesForChannel(realm: Realm, channelId: String): List<Course> = realm
        .where(Course::class.java)
        .equalTo("channelId", channelId)
        .equalTo("external", false)
        .greaterThan("startDate", Date())
        .sort("startDate", Sort.ASCENDING)
        .findAll()

    fun listCoursesForChannel(channelId: String, realm: Realm, listener: RealmChangeListener<RealmResults<Course>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val courseListPromise = realm
            .where(Course::class.java)
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .sort("startDate", Sort.DESCENDING)
            .findAllAsync()

        courseListPromise.addChangeListener(listener)

        return courseListPromise
    }

}
