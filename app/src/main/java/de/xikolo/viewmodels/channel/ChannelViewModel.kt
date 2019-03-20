package de.xikolo.viewmodels.channel

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.dao.ChannelsDao
import de.xikolo.models.dao.CoursesDao
import de.xikolo.network.jobs.GetChannelWithCoursesJob
import de.xikolo.utils.SectionList
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.main.CourseListViewModel

class ChannelViewModel(val channelId: String) : BaseViewModel() {

    private val channelsDao = ChannelsDao(realm)
    private val coursesDao = CoursesDao(realm)
    private val courseListViewModel = CourseListViewModel(CourseListFilter.ALL)

    val channel: LiveData<Channel> by lazy {
        channelsDao.channel(channelId)
    }

    val courses: LiveData<List<Course>> = courseListViewModel.courses

    fun buildContentList(channel: Channel): SectionList<String, List<Course>> {
        val contentList = SectionList<String, List<Course>>()
        contentList.add(channel.description, ArrayList())
        var subList: List<Course>
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            subList = coursesDao.futureCoursesForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.getInstance().getString(R.string.header_future_courses),
                    subList
                )
            }
            subList = coursesDao.currentAndPastCoursesForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.getInstance().getString(R.string.header_self_paced_courses),
                    subList
                )
            }
        } else {
            subList = coursesDao.currentAndFutureCoursesForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                    subList
                )
            }
            subList = coursesDao.pastCoursesForChannel(channelId)
            if (subList.isNotEmpty()) {
                contentList.add(
                    App.getInstance().getString(R.string.header_self_paced_courses),
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

