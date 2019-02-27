package de.xikolo.viewmodels

import androidx.lifecycle.LiveData
import de.xikolo.BuildConfig
import de.xikolo.config.BuildFlavor
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.dao.ChannelsDao
import de.xikolo.models.dao.CoursesDao
import de.xikolo.network.jobs.ListChannelsWithCoursesJob
import de.xikolo.viewmodels.base.BaseViewModel

class ChannelListViewModel : BaseViewModel() {

    private val channelsDao = ChannelsDao(realm)
    private val coursesDao = CoursesDao(realm)

    val channels: LiveData<List<Channel>> by lazy {
        channelsDao.channels()
    }

    fun buildCourseLists(channelList: List<Channel>): List<List<Course>> {
        val courseLists = mutableListOf<List<Course>>()
        for (channel in channelList) {
            val courseList = mutableListOf<Course>()
            if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                courseList.addAll(coursesDao.futureCoursesForChannel(channel.id))
                courseList.addAll(coursesDao.currentAndPastCoursesForChannel(channel.id))
            } else {
                courseList.addAll(coursesDao.currentAndFutureCoursesForChannel(channel.id))
                courseList.addAll(coursesDao.pastCoursesForChannel(channel.id))
            }
            courseLists.add(courseList)
        }
        return courseLists
    }

    override fun onFirstCreate() {
        requestChannelList(false)
    }

    override fun onRefresh() {
        requestChannelList(true)
    }

    private fun requestChannelList(userRequest: Boolean) {
        ListChannelsWithCoursesJob(networkState, userRequest).run()
    }
}
