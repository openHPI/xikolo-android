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

    fun dates(): LiveData<List<CourseDate>> =
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

    fun datesToday(): List<CourseDate> =
        realm
            .where<CourseDate>()
            .between("date", Date(), DateUtil.todaysMidnight())
            .findAll()

    fun datesNextSevenDays(): List<CourseDate> =
        realm
            .where<CourseDate>()
            .between("date", Date(), DateUtil.nextSevenDays())
            .findAll()

    fun datesInFuture(): List<CourseDate> =
        realm
            .where<CourseDate>()
            .greaterThan("date", Date())
            .findAll()

}
