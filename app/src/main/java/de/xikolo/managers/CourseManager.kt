package de.xikolo.managers

import de.xikolo.models.Course
import de.xikolo.models.Enrollment
import de.xikolo.models.SectionProgress
import de.xikolo.network.jobs.*
import de.xikolo.network.jobs.base.RequestJobCallback
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults

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

    fun requestCourseWithSections(courseId: String, callback: RequestJobCallback) {
        GetCourseWithSectionsJob(courseId, callback).run()
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
