package de.xikolo.controllers.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.config.GlideApp
import de.xikolo.models.Course

abstract class BaseCourseListAdapter<M>(val fragment: Fragment, private val onCourseButtonClickListener: OnCourseButtonClickListener?) : BaseMetaRecyclerViewAdapter<M, Course>() {

    companion object {
        val TAG: String = BaseCourseListAdapter::class.java.simpleName
    }

    protected fun createCourseViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CourseViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_course_list, parent, false)
        )
    }

    protected fun bindCourseViewHolder(holder: CourseViewHolder, position: Int) {
        val course = super.contentList.get(position) as Course

        holder.textDate.text = course.formattedDate
        holder.textTitle.text = course.title
        holder.textTeacher.text = course.teachers
        holder.textLanguage.text = course.formattedLanguage
        holder.textTeacher.visibility = if (course.teachers.isNullOrEmpty()) View.GONE else View.VISIBLE

        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            holder.textBanner.visibility = View.GONE
        }

        GlideApp.with(fragment).load(course.imageUrl).into(holder.image)

        holder.buttonCourseAction.isEnabled = true

        holder.buttonCourseDetails.visibility = View.VISIBLE
        holder.buttonCourseDetails.setOnClickListener { onCourseButtonClickListener?.onDetailButtonClicked(course.id) }

        if (course.isEnrolled && course.accessible) {
            holder.layout.setOnClickListener { onCourseButtonClickListener?.onContinueButtonClicked(course.id) }

            holder.buttonCourseAction.text = App.getInstance().getString(R.string.btn_continue_course)
            holder.buttonCourseAction.setOnClickListener { onCourseButtonClickListener?.onContinueButtonClicked(course.id) }

            holder.buttonCourseDetails.visibility = View.GONE

        } else if (course.isEnrolled && !course.accessible) {
            holder.layout.setOnClickListener { onCourseButtonClickListener?.onDetailButtonClicked(course.id) }

            holder.buttonCourseAction.text = App.getInstance().getString(R.string.btn_starts_soon)
            holder.buttonCourseAction.isEnabled = false
            holder.buttonCourseAction.isClickable = false

        } else {
            holder.layout.setOnClickListener { onCourseButtonClickListener?.onDetailButtonClicked(course.id) }

            holder.buttonCourseAction.text = App.getInstance().getString(R.string.btn_enroll)
            holder.buttonCourseAction.setOnClickListener { onCourseButtonClickListener?.onEnrollButtonClicked(course.id) }
        }
    }

    interface OnCourseButtonClickListener {

        fun onEnrollButtonClicked(courseId: String)

        fun onContinueButtonClicked(courseId: String)

        fun onDetailButtonClicked(courseId: String)
    }

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.container)
        lateinit var layout: ViewGroup

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.textTeacher)
        lateinit var textTeacher: TextView

        @BindView(R.id.textDate)
        lateinit var textDate: TextView

        @BindView(R.id.textLanguage)
        lateinit var textLanguage: TextView

        @BindView(R.id.textDescription)
        lateinit var textDescription: TextView

        @BindView(R.id.imageView)
        lateinit var image: ImageView

        @BindView(R.id.button_course_action)
        lateinit var buttonCourseAction: Button

        @BindView(R.id.button_course_details)
        lateinit var buttonCourseDetails: Button

        @BindView(R.id.textBanner)
        lateinit var textBanner: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

}
