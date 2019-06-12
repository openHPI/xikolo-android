package de.xikolo.controllers.course

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.video.VideoStreamPlayerActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.models.Course
import de.xikolo.utils.DisplayUtil
import de.xikolo.utils.MarkdownUtil
import de.xikolo.viewmodels.course.DescriptionViewModel
import de.xikolo.views.CustomSizeImageView

class DescriptionFragment : NetworkStateFragment<DescriptionViewModel>() {

    companion object {
        val TAG: String = DescriptionFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.videoPreview)
    internal lateinit var videoPreview: ViewGroup

    @BindView(R.id.videoThumbnail)
    lateinit var imageVideoThumbnail: CustomSizeImageView

    @BindView(R.id.durationText)
    lateinit var textDuration: TextView

    @BindView(R.id.playButton)
    lateinit var viewPlay: View

    @BindView(R.id.courseImage)
    internal lateinit var courseImage: ImageView

    @BindView(R.id.text_teacher)
    internal lateinit var textTeacher: TextView

    @BindView(R.id.text_date)
    internal lateinit var textDate: TextView

    @BindView(R.id.text_language)
    internal lateinit var textLanguage: TextView

    @BindView(R.id.text_description)
    internal lateinit var textDescription: TextView

    override val layoutResource = R.layout.content_course_description

    override fun createViewModel(): DescriptionViewModel {
        return DescriptionViewModel(courseId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textDuration.visibility = View.GONE

        activity?.let {
            val thumbnailSize: Point = DisplayUtil.getVideoThumbnailSize(it)
            imageVideoThumbnail.setDimensions(thumbnailSize.x, thumbnailSize.y)
        }

        viewModel.course
            .observe(this) {
                showDescription(it)
            }
    }

    private fun showDescription(course: Course) {
        when {
            course.teaserStream != null
                && (course.teaserStream.hdUrl != null || course.teaserStream.sdUrl != null) -> {
                courseImage.visibility = View.GONE
                videoPreview.visibility = View.VISIBLE

                viewPlay.setOnClickListener {
                    activity?.let {
                        startActivity(
                            VideoStreamPlayerActivityAutoBundle.builder(course.teaserStream)
                                .parentIntent(it.intent)
                                .overrideActualParent(true)
                                .build(it)
                        )
                    }
                }
            }
            course.imageUrl != null                                                         -> {
                videoPreview.visibility = View.GONE
                courseImage.visibility = View.VISIBLE
            }
            else                                                                            -> {
                courseImage.visibility = View.GONE
                videoPreview.visibility = View.GONE
            }
        }

        when {
            course.teaserStream != null && course.teaserStream.thumbnailUrl != null -> GlideApp.with(this)
                .load(course.teaserStream.thumbnailUrl)
                .override(imageVideoThumbnail.forcedWidth, imageVideoThumbnail.forcedHeight)
                .into(imageVideoThumbnail)
            course.imageUrl != null                  -> GlideApp.with(this)
                .load(course.imageUrl)
                .into(courseImage)
        }

        textDate.text = course.formattedDate
        textLanguage.text = course.formattedLanguage
        MarkdownUtil.formatAndSet(course.description, textDescription)

        if (!course.teachers.isNullOrEmpty()) {
            textTeacher.text = course.teachers
        } else {
            textTeacher.visibility = View.GONE
        }
        showContent()
    }

}
