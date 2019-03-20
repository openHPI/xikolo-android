package de.xikolo.controllers.course

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.models.Course
import de.xikolo.utils.MarkdownUtil
import de.xikolo.viewmodels.base.observe
import de.xikolo.viewmodels.course.DescriptionViewModel

class DescriptionFragment : NetworkStateFragment<DescriptionViewModel>() {

    companion object {
        val TAG: String = DescriptionFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.layout_header)
    internal lateinit var imageView: ImageView

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
        viewModel.course
            .observe(this) {
                showDescription(it)
            }
    }

    private fun showDescription(course: Course) {
        if (course.imageUrl != null) {
            GlideApp.with(this).load(course.imageUrl).into(imageView)
        } else {
            imageView.visibility = View.GONE
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
