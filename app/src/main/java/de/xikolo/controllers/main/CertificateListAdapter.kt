package de.xikolo.controllers.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.controllers.helper.DownloadViewHelper
import de.xikolo.models.Course
import de.xikolo.models.DownloadAsset
import de.xikolo.models.Enrollment
import java.util.*

class CertificateListAdapter(private val fragment: CertificateListFragment, private val callback: OnCertificateCardClickListener) : RecyclerView.Adapter<CertificateListAdapter.CertificateViewHolder>() {

    companion object {
        val TAG = CertificateListAdapter::class.java.simpleName!!
    }

    private var courseList: MutableList<Course> = ArrayList()

    fun update(courseList: MutableList<Course>) {
        this.courseList = courseList
        notifyDataSetChanged()
    }

    fun clear() {
        courseList.clear()
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_certificate_list, parent, false)
        return CertificateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) {
        val course = courseList[position]

        holder.textTitle.text = course.title

        holder.header.setOnClickListener { _ -> callback.onCourseClicked(course.id) }

        if (course.imageUrl != null)
            GlideApp.with(App.getInstance()).load(course.imageUrl).into(holder.courseImage)
        else
            holder.courseImage.visibility = View.GONE

        holder.container.removeAllViews()

        val enrollment: Enrollment? = Enrollment.getForCourse(course.id)
        if (enrollment != null) {
            if (course.certificates.confirmationOfParticipation.available) {
                val downloadViewHelper = DownloadViewHelper(
                    fragment.activity!!,
                    DownloadAsset.Certificate.ConfirmationOfParticipation(
                        enrollment.certificateUrls.confirmationOfParticipation,
                        course
                    ),
                    App.getInstance().getString(R.string.course_confirmation_of_participation),
                    null,
                    App.getInstance().getString(R.string.course_certificate_not_achieved)
                )
                holder.container.addView(downloadViewHelper.view)
                downloadViewHelper.openFileAsPdf()
            }

            if (course.certificates.recordOfAchievement.available) {
                val downloadViewHelper = DownloadViewHelper(
                    fragment.activity!!,
                    DownloadAsset.Certificate.RecordOfAchievement(
                        enrollment.certificateUrls.recordOfAchievement,
                        course
                    ),
                    App.getInstance().getString(R.string.course_record_of_achievement),
                    null,
                    App.getInstance().getString(R.string.course_certificate_not_achieved)
                )
                holder.container.addView(downloadViewHelper.view)
                downloadViewHelper.openFileAsPdf()
            }

            if (course.certificates.qualifiedCertificate.available) {
                val downloadViewHelper = DownloadViewHelper(
                    fragment.activity!!,
                    DownloadAsset.Certificate.QualifiedCertificate(
                        enrollment.certificateUrls.qualifiedCertificate,
                        course
                    ),
                    App.getInstance().getString(R.string.course_qualified_certificate),
                    null,
                    App.getInstance().getString(R.string.course_certificate_not_achieved)
                )
                holder.container.addView(downloadViewHelper.view)
                downloadViewHelper.openFileAsPdf()
            }
        }
    }

    interface OnCertificateCardClickListener {

        fun onCourseClicked(courseId: String)
    }

    class CertificateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.certificateHeader)
        lateinit var header: ViewGroup

        @BindView(R.id.courseImage)
        lateinit var courseImage: ImageView

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.container)
        lateinit var container: LinearLayout

        init {
            ButterKnife.bind(this, view)
        }
    }
}
