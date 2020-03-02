package de.xikolo.viewmodels.channel

import androidx.lifecycle.LiveData
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.VideoStream
import de.xikolo.models.dao.ChannelDao
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.GetChannelWithCoursesJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.MetaSectionList
import de.xikolo.viewmodels.base.BaseViewModel
import de.xikolo.viewmodels.shared.CourseListDelegate
import de.xikolo.viewmodels.shared.EnrollmentDelegate

class ChannelViewModel(private val channelId: String) : BaseViewModel() {

    private val courseListDelegate = CourseListDelegate(realm)
    private val enrollmentDelegate = EnrollmentDelegate(realm)

    private val channelsDao = ChannelDao(realm)

    val channel: LiveData<Channel> by lazy {
        channelsDao.find(channelId)
    }

    val courses = courseListDelegate.courses

    fun buildContentList(channel: Channel): MetaSectionList<String, Triple<String?, VideoStream?, String?>, List<Course>> {
        val contentList = MetaSectionList<String, Triple<String?, VideoStream?, String?>, List<Course>>(Triple<String?, VideoStream?, String>(channel.description, channel.stageStream, channel.imageUrl))
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

    fun enroll(courseId: String, networkState: NetworkStateLiveData) {
        enrollmentDelegate.createEnrollment(courseId, networkState, true)
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

