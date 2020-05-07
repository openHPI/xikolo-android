package de.xikolo.viewmodels.shared

import androidx.lifecycle.LiveData
import de.xikolo.models.CourseDate
import de.xikolo.models.dao.DateDao
import de.xikolo.network.jobs.ListDatesJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class DateListDelegate(realm: Realm) {

    private val dateDao = DateDao(realm)

    val dates: LiveData<List<CourseDate>> by lazy {
        dateDao.all()
    }

    fun datesForCourse(courseId: String): LiveData<List<CourseDate>> {
        return dateDao.allForCourse(courseId)
    }

    fun requestDateList(networkState: NetworkStateLiveData, userRequest: Boolean) {
        ListDatesJob(networkState, userRequest).run()
    }
}
