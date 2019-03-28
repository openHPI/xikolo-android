package de.xikolo.viewmodels.channel

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.dao.ChannelDao
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.GetChannelWithCoursesJob
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.main.CourseListViewModel

class ChannelViewModel(val channelId: String) : BaseViewModel() {

    private val channelsDao = ChannelDao(realm)
    private val courseListViewModel = CourseListViewModel(CourseListFilter.ALL)

    val channel: LiveData<Channel> by lazy {
        channelsDao.find(channelId)
    }

    val courses: LiveData<List<Course>> = courseListViewModel.courses

    fun buildContentList(channel: Channel): MetaSectionList<String, String, List<Course>> {
        val contentList = MetaSectionList<String, String, List<Course>>(channel.description)
        var subList: List<Course>
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            subList = CourseDao.Unmanaged.allFutureForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.instance.getString(R.string.header_future_courses),
                    subList
                )
            }
            subList = CourseDao.Unmanaged.allCurrentAndPastForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.instance.getString(R.string.header_self_paced_courses),
                    subList
                )
            }
        } else {
            subList = CourseDao.Unmanaged.allCurrentAndFutureForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.instance.getString(R.string.header_current_and_upcoming_courses),
                    subList
                )
            }
            subList = CourseDao.Unmanaged.allPastForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.instance.getString(R.string.header_self_paced_courses),
                    subList
                )
            }
        }
        return contentList
    }

    override fun onFirstCreate() {
        requestChannel(false)
    }

    override fun onRefresh() {
        requestChannel(true)
    }

    private fun requestChannel(userRequest: Boolean) {
        GetChannelWithCoursesJob(channelId, networkState, userRequest).run()
    }
}

