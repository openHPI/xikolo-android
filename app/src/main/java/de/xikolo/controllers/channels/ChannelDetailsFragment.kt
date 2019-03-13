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
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.managers.CourseManager
import de.xikolo.managers.UserManager
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.utils.SectionList
import de.xikolo.viewmodels.ChannelViewModel
import de.xikolo.viewmodels.base.observe
import de.xikolo.views.AutofitRecyclerView
import de.xikolo.views.SpaceItemDecoration

class ChannelDetailsFragment : NetworkStateFragment<ChannelViewModel>() {

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

    private val courseManager = CourseManager()

    @BindView(R.id.course_list)
    internal lateinit var recyclerView: AutofitRecyclerView

    private lateinit var contentListAdapter: ChannelCourseListAdapter

    override val layoutResource = R.layout.content_channel_details

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
        })

        recyclerView.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (contentListAdapter.isHeader(position)) recyclerView.spanCount else 1
            }
        }

        recyclerView.adapter = contentListAdapter

        recyclerView.addItemDecoration(SpaceItemDecoration(
            App.getInstance().resources.getDimensionPixelSize(R.dimen.card_horizontal_margin),
            App.getInstance().resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
            false,
            object : SpaceItemDecoration.RecyclerViewInfo {
                override fun isHeader(position: Int): Boolean {
                    return contentListAdapter.isHeader(position)
                }

                override fun getSpanCount(): Int {
                    return recyclerView.spanCount
                }

                override fun getItemCount(): Int {
                    return contentListAdapter.itemCount
                }
            }))

        viewModel.channel
            .observe(this) {
                updateView(it)
            }

        viewModel.courses
            .observe(this) {
                viewModel.channel.value?.let {
                    showContentList(
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
        }

        contentListAdapter.setButtonColor(channel.colorOrDefault)
    }

    private fun showContentList(contents: SectionList<String, List<Course>>) {
        contentListAdapter.update(contents)

        if (scrollToCoursePosition >= 0) {
            try {
                val activity = activity as ChannelDetailsActivity?
                activity?.appBarLayout?.setExpanded(false)

                var headerCount = 0
                for (i in 0 until scrollToCoursePosition) {
                    if (contentListAdapter.getItemViewType(i) == ChannelCourseListAdapter.ITEM_VIEW_TYPE_HEADER
                        || contentListAdapter.getItemViewType(i) == ChannelCourseListAdapter.ITEM_VIEW_TYPE_META) {
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
        courseManager.createEnrollment(courseId, object : RequestJobCallback() {
            public override fun onSuccess() {
                if (view != null) {
                    hideAnyProgress()
                    val course = Course.get(courseId)
                    if (course.accessible) {
                        enterCourse(courseId)
                    }
                }
            }

            public override fun onError(code: RequestJobCallback.ErrorCode) {
                if (view != null) {
                    hideAnyProgress()
                    if (code === ErrorCode.NO_NETWORK) {
                        showNetworkRequired()
                    } else if (code === RequestJobCallback.ErrorCode.NO_AUTH) {
                        showLoginRequired()
                        openLogin()
                    }
                }
            }
        })
    }

    private fun enterCourse(courseId: String) {
        if (!UserManager.isAuthorized) {
            showLoginRequired()
            openLogin()
        } else {
            val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(App.getInstance())
            startActivity(intent)
        }
    }

    private fun enterCourseDetails(courseId: String) {
        val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(App.getInstance())
        startActivity(intent)
    }

    private fun openLogin() {
        val intent = LoginActivityAutoBundle.builder().build(App.getInstance())
        startActivity(intent)
    }

}
