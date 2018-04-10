package de.xikolo.managers

import de.xikolo.jobs.*
import de.xikolo.jobs.base.RequestJobCallback
import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.models.SectionProgress
import io.realm.*
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

    fun listCourses(realm: Realm, listener: RealmChangeListener<RealmResults<Course>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val courseListPromise = realm
                .where(Course::class.java)
                .equalTo("external", false)
                .sort("startDate", Sort.DESCENDING)
                .findAllAsync()

        courseListPromise.addChangeListener(listener)

        return courseListPromise
    }

    fun listEnrolledCourses(realm: Realm, listener: RealmChangeListener<RealmResults<Course>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val courseListPromise = realm
                .where(Course::class.java)
                .equalTo("external", false)
                .isNotNull("enrollmentId")
                .sort("startDate", Sort.DESCENDING)
                .findAllAsync()

        courseListPromise.addChangeListener(listener)

        return courseListPromise
    }

    fun searchCourses(query: String, withEnrollment: Boolean, realm: Realm, listener: RealmChangeListener<RealmResults<Course>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        var dbQuery = realm
                .where(Course::class.java)
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

        val courseListPromise = dbQuery
                .sort("startDate", Sort.DESCENDING)
                .findAllAsync()

        courseListPromise.addChangeListener(listener)

        return courseListPromise
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

    fun listCurrentAndFutureCourses(realm: Realm): List<Course> = realm
            .where(Course::class.java)
            .equalTo("external", false)
            .greaterThanOrEqualTo("endDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun listCurrentAndPastCourses(realm: Realm): List<Course> = realm
            .where(Course::class.java)
            .equalTo("external", false)
            .lessThanOrEqualTo("startDate", Date())
            .sort("startDate", Sort.DESCENDING)
            .findAll()

    fun listPastCourses(realm: Realm): List<Course> = realm
            .where(Course::class.java)
            .equalTo("external", false)
            .lessThan("endDate", Date())
            .sort("startDate", Sort.DESCENDING)
            .findAll()

    fun listFutureCourses(realm: Realm): List<Course> =realm
            .where(Course::class.java)
            .equalTo("external", false)
            .greaterThan("startDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun listCurrentAndFutureCoursesForChannel(realm: Realm, channelId: String): List<Course> = realm
            .where(Course::class.java)
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .greaterThanOrEqualTo("endDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun listCurrentAndPastCoursesForChannel (realm: Realm, channelId: String): List<Course> = realm
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

    fun listFutureCoursesForChannel(realm: Realm, channelId: String): List<Course> =realm
            .where(Course::class.java)
            .equalTo("channelId", channelId)
            .equalTo("external", false)
            .greaterThan("startDate", Date())
            .sort("startDate", Sort.ASCENDING)
            .findAll()

    fun listCurrentAndPastCoursesWithEnrollment(realm: Realm): List<Course> = realm
        .where(Course::class.java)
        .equalTo("external", false)
        .isNotNull("enrollmentId")
        .lessThanOrEqualTo("startDate", Date())
        .sort("startDate", Sort.DESCENDING)
        .findAll()

    fun listFutureCoursesWithEnrollment(realm: Realm): List<Course> = realm
        .where(Course::class.java)
        .equalTo("external", false)
        .isNotNull("enrollmentId")
        .greaterThan("startDate", Date())
        .sort("startDate", Sort.ASCENDING)
        .findAll()

    fun listSectionProgressesForCourse(courseId: String, realm: Realm, listener: RealmChangeListener<RealmResults<SectionProgress>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val spListPromise = realm
                .where(SectionProgress::class.java)
                .equalTo("courseProgressId", courseId)
                .sort("position")
                .findAllAsync()

        spListPromise.addChangeListener(listener)

        return spListPromise
    }

    fun listCoursesForChannel(channelId: String, realm: Realm, listener: RealmChangeListener<RealmResults<Course>>?): RealmResults<*> {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        val courseListPromise = realm
                .where(Course::class.java)
                .equalTo("channelId", channelId)
                .equalTo("external", false)
                //.greaterThanOrEqualTo("endDate", Date())
                //.sort("startDate", Sort.ASCENDING) //ToDO which courses shall be displayed?
                .findAllAsync()

        courseListPromise.addChangeListener(listener)

        return courseListPromise
    }

    fun countEnrollments(realm: Realm): Long {
        return realm.where(Enrollment::class.java).count()
    }

    fun requestCourse(courseId: String, callback: RequestJobCallback) {
        GetCourseJob(courseId, callback).run()
    }

    fun requestCourseWithSections(courseId: String, callback: RequestJobCallback) {
        GetCourseWithSectionsJob(courseId, callback).run()
    }

    fun requestCourseList(callback: RequestJobCallback) {
        ListCoursesJob(callback).run()
    }

    fun requestEnrollmentList(callback: RequestJobCallback) {
        ListEnrollmentsJob(callback).run()
    }

    fun createEnrollment(courseId: String, callback: RequestJobCallback) {
        CreateEnrollmentJob(courseId, callback).run()
    }

    fun deleteEnrollment(id: String, callback: RequestJobCallback) {
        DeleteEnrollmentJob(id, callback).run()
    }

    fun requestCourseProgressWithSections(courseId: String, callback: RequestJobCallback) {
        GetCourseProgressWithSectionsJob(courseId, callback).run()
    }

}
