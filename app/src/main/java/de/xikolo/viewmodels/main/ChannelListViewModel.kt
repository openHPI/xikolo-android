package de.xikolo.viewmodels.main

import androidx.lifecycle.LiveData
import de.xikolo.config.Feature
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.dao.ChannelDao
import de.xikolo.models.dao.CourseDao
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.ChannelListDelegate

class ChannelListViewModel : BaseViewModel() {

    private val channelsDao = ChannelDao(realm)
    private val channelListDelegate = ChannelListDelegate(realm)

    val channels: LiveData<List<Channel>> by lazy {
        channelsDao.all()
    }

    fun buildCourseLists(channelList: List<Channel>): List<List<Course>> {
        val courseLists = mutableListOf<List<Course>>()
        for (channel in channelList) {
            val courseList = mutableListOf<Course>()
            if (Feature.enabled("merge_current_and_future_courses")) {
                courseList.addAll(CourseDao.Unmanaged.allCurrentAndFutureForChannel(channel.id))
                courseList.addAll(CourseDao.Unmanaged.allPastForChannel(channel.id))
            } else {
                courseList.addAll(CourseDao.Unmanaged.allFutureForChannel(channel.id))
                courseList.addAll(CourseDao.Unmanaged.allCurrentAndPastForChannel(channel.id))
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
        channelListDelegate.requestChannels(networkState, userRequest)
    }
}
