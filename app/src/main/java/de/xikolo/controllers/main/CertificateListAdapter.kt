package de.xikolo.controllers.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.helper.DownloadViewHelper
import de.xikolo.models.Course
import de.xikolo.models.DownloadAsset
import de.xikolo.models.dao.EnrollmentDao

class CertificateListAdapter(private val fragment: CertificateListFragment, private val callback: OnCertificateCardClickListener) : RecyclerView.Adapter<CertificateListAdapter.CertificateViewHolder>() {

    companion object {
        val TAG: String = CertificateListAdapter::class.java.simpleName
    }

    private val courseList: MutableList<Course> = mutableListOf()

    fun update(courseList: List<Course>) {
        this.courseList.clear()
        this.courseList.addAll(courseList)
        notifyDataSetChanged()
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

        holder.textTitle.setOnClickListener { _ -> callback.onCourseClicked(course.id) }

        holder.container.removeAllViews()

        fragment.activity?.let { activity ->
            EnrollmentDao.Unmanaged.findForCourse(course.id)?.let { enrollment ->
                if (course.certificates.confirmationOfParticipation.available) {
                    val dvh = DownloadViewHelper(
                        activity,
                        DownloadAsset.Certificate.ConfirmationOfParticipation(
                            enrollment.certificates.confirmationOfParticipationUrl,
                            course
                        ),
                        App.instance.getString(R.string.course_confirmation_of_participation),
                        null,
                        App.instance.getString(R.string.course_certificate_not_achieved)
                    )

                    holder.container.addView(dvh.view)
                }

                if (course.certificates.recordOfAchievement.available) {
                    val dvh = DownloadViewHelper(
                        activity,
                        DownloadAsset.Certificate.RecordOfAchievement(
                            enrollment.certificates.recordOfAchievementUrl,
                            course
                        ),
                        App.instance.getString(R.string.course_record_of_achievement),
                        null,
                        App.instance.getString(R.string.course_certificate_not_achieved)
                    )

                    holder.container.addView(dvh.view)
                }

                if (course.certificates.qualifiedCertificate.available) {
                    val dvh = DownloadViewHelper(
                        activity,
                        DownloadAsset.Certificate.QualifiedCertificate(
                            enrollment.certificates.qualifiedCertificateUrl,
                            course
                        ),
                        App.instance.getString(R.string.course_qualified_certificate),
                        null,
                        App.instance.getString(R.string.course_certificate_not_achieved)
                    )

                    holder.container.addView(dvh.view)
                }
            }
        }
    }

    interface OnCertificateCardClickListener {
        fun onCourseClicked(courseId: String)
    }

    class CertificateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.container)
        lateinit var container: LinearLayout

        init {
            ButterKnife.bind(this, view)
        }

    }

}
