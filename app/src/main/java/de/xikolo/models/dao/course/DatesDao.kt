package de.xikolo.models.dao.course

import androidx.lifecycle.LiveData
import de.xikolo.models.CourseDate
import de.xikolo.models.dao.base.BaseDao
import de.xikolo.utils.DateUtil
import de.xikolo.viewmodels.base.asLiveData
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

class DatesDao(realm: Realm) : BaseDao(realm) {

    fun futureDates(): LiveData<List<CourseDate>> =
        realm
            .where<CourseDate>()
            .greaterThanOrEqualTo("date", Date())
            .sort("date", Sort.ASCENDING)
            .findAllAsync()
            .asLiveData()

    fun nextDate(): CourseDate? =
        realm
            .where<CourseDate>()
            .greaterThanOrEqualTo("date", Date())
            .sort("date", Sort.ASCENDING)
            .findFirst()

    fun dateCountToday(): Long =
        realm
            .where<CourseDate>()
            .between("date", Date(), DateUtil.todaysMidnight())
            .count()

    fun dateCountNextSevenDays(): Long =
        realm
            .where<CourseDate>()
            .between("date", Date(), DateUtil.nextSevenDays())
            .count()

    fun dateCountFuture(): Long =
        realm
            .where<CourseDate>()
            .greaterThan("date", Date())
            .count()

}
