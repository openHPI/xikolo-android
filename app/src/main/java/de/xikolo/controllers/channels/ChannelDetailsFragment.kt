package de.xikolo.controllers.channels

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.BaseCourseListAdapter
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.controllers.helper.LoginHelper
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.extensions.observeOnce
import de.xikolo.managers.UserManager
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.VideoStream
import de.xikolo.models.dao.CourseDao
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.MetaSectionList
import de.xikolo.utils.extensions.openUrl
import de.xikolo.viewmodels.channel.ChannelViewModel
import de.xikolo.views.AutofitRecyclerView
import de.xikolo.views.SpaceItemDecoration

class ChannelDetailsFragment : ViewModelFragment<ChannelViewModel>() {

    companion object {
        val TAG: String = ChannelDetailsFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var channelId: String

    // -1 do not scroll to course
    @AutoBundleField(required = false)
    var scrollToCoursePosition = -1

    @BindView(R.id.layout_header)
    internal lateinit var layoutHeader: FrameLayout

    @BindView(R.id.image_channel)
    internal lateinit var imageChannel: ImageView

    @BindView(R.id.text_title)
    internal lateinit var textTitle: TextView

    @BindView(R.id.course_list)
    internal lateinit var recyclerView: AutofitRecyclerView

    private lateinit var contentListAdapter: ChannelCourseListAdapter

    override val layoutResource = R.layout.fragment_channel_details

    override fun createViewModel(): ChannelViewModel {
        return ChannelViewModel(channelId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentListAdapter = ChannelCourseListAdapter(this, object : BaseCourseListAdapter.OnCourseButtonClickListener {
            override fun onEnrollButtonClicked(courseId: String) {
                enroll(courseId)
            }

            override fun onContinueButtonClicked(courseId: String) {
                enterCourse(courseId)
            }

            override fun onDetailButtonClicked(courseId: String) {
                enterCourseDetails(courseId)
            }

            override fun onExternalButtonClicked(course: Course) {
                enterExternalCourse(course)
            }
        })

        recyclerView.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (contentListAdapter.isHeader(position)) recyclerView.spanCount else 1
            }
        }

        recyclerView.adapter = contentListAdapter

        recyclerView.addItemDecoration(SpaceItemDecoration(
            App.instance.resources.getDimensionPixelSize(R.dimen.card_horizontal_margin),
            App.instance.resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
            false,
            object : SpaceItemDecoration.RecyclerViewInfo {
                override fun isHeader(position: Int): Boolean {
                    return contentListAdapter.isHeader(position)
                }

                override val spanCount: Int
                    get() = recyclerView.spanCount

                override val itemCount: Int
                    get() = contentListAdapter.itemCount
            }
        ))

        viewModel.channel
            .observe(viewLifecycleOwner) {
                updateView(it)
                updateContentList(
                    viewModel.buildContentList(it)
                )
            }

        viewModel.courses
            .observe(viewLifecycleOwner) {
                viewModel.channel.value?.let {
                    updateContentListAndScroll(
                        viewModel.buildContentList(it)
                    )
                }
            }
    }

    private fun updateView(channel: Channel) {
        if (activity is ChannelDetailsActivity) {
            layoutHeader.visibility = View.GONE
        } else {
            GlideApp.with(this).load(channel.imageUrl).into(imageChannel)
            textTitle.text = channel.title
            layoutHeader.visibility = View.VISIBLE
        }

        contentListAdapter.setThemeColor(channel.colorOrDefault)
        showContent()
    }

    private fun updateContentList(contents: MetaSectionList<String, Triple<String?, VideoStream?, String?>, List<Course>>) {
        contentListAdapter.update(contents)
    }

    private fun updateContentListAndScroll(contents: MetaSectionList<String, Triple<String?, VideoStream?, String?>, List<Course>>) {
        updateContentList(contents)

        if (scrollToCoursePosition >= 0) {
            try {
                val activity = activity as ChannelDetailsActivity?
                activity?.appBarLayout?.setExpanded(false)

                var headerCount = 0
                for (i in 0 until scrollToCoursePosition) {
                    if (contentListAdapter.isHeader(i)
                        || contentListAdapter.isMetaItem(i)) {
                        headerCount++
                    }
                }

                recyclerView.smoothScrollToPosition(scrollToCoursePosition + headerCount)
                scrollToCoursePosition = -1

                showContent()
            } catch (ignored: Exception) {
                showErrorMessage()
            }
        }
        showContent()
    }

    private fun enroll(courseId: String) {
        showBlockingProgress()

        val enrollmentCreationNetworkState = NetworkStateLiveData()
        enrollmentCreationNetworkState
            .observeOnce(viewLifecycleOwner) {
                when (it.code) {
                    NetworkCode.SUCCESS    -> {
                        hideAnyProgress()
                        val course = CourseDao.Unmanaged.find(courseId)
                        if (course?.accessible == true) {
                            enterCourse(courseId)
                        }
                        true
                    }
                    NetworkCode.NO_NETWORK -> {
                        hideAnyProgress()
                        showNetworkRequired()
                        true
                    }
                    NetworkCode.NO_AUTH    -> {
                        hideAnyProgress()
                        showLoginRequired()
                        openLogin()
                        true
                    }
                    else                   -> false
                }
            }

        viewModel.enroll(courseId, enrollmentCreationNetworkState)
    }

    private fun enterCourse(courseId: String) {
        if (!UserManager.isAuthorized) {
            showLoginRequired()
            openLogin()
        } else {
            val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(App.instance)
            startActivity(intent)
        }
    }

    private fun enterCourseDetails(courseId: String) {
        val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(App.instance)
        startActivity(intent)
    }

    private fun enterExternalCourse(course: Course) {
        activity?.openUrl(course.externalUrl)
    }

    private fun openLogin() {
        val intent = LoginHelper.loginIntent(App.instance)
        startActivity(intent)
    }

}
