package de.xikolo.controllers.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.models.Course
import java.util.*

class CertificateListAdapter(private val callback: OnCertificateCardClickListener) : RecyclerView.Adapter<CertificateListAdapter.CertificateViewHolder>() {

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

        if (!course.certificates.qualifiedCertificate.available)
            holder.qualifiedCertificate.visibility = View.GONE
        else
            holder.qualifiedCertificate.visibility = View.VISIBLE

        if (course.certificates.qualifiedCertificate.url != null) {
            holder.qualifiedCertificateButton.isEnabled = true
            holder.qualifiedCertificateButton.setOnClickListener { v -> callback.onViewCertificateClicked(course.certificates.qualifiedCertificate!!.url) }
        } else
            holder.qualifiedCertificateButton.isEnabled = false


        if (!course.certificates.recordOfAchievement.available)
            holder.recordOfAchievement.visibility = View.GONE
        else
            holder.recordOfAchievement.visibility = View.VISIBLE

        if (course.certificates.recordOfAchievement.url != null) {
            holder.recordOfAchievementButton.isEnabled = true
            holder.recordOfAchievementButton.setOnClickListener { _ -> callback.onViewCertificateClicked(course.certificates.recordOfAchievement!!.url) }
        } else
            holder.recordOfAchievementButton.isEnabled = false


        if (!course.certificates.confirmationOfParticipation.available)
            holder.confirmationOfParticipation.visibility = View.GONE
        else
            holder.confirmationOfParticipation.visibility = View.VISIBLE

        if (course.certificates.confirmationOfParticipation.url != null) {
            holder.confirmationOfParticipationButton.isEnabled = true
            holder.confirmationOfParticipationButton.setOnClickListener { _ -> callback.onViewCertificateClicked(course.certificates.confirmationOfParticipation!!.url) }
        } else
            holder.confirmationOfParticipationButton.isEnabled = false
    }

    interface OnCertificateCardClickListener {

        fun onViewCertificateClicked(url: String?)

        fun onCourseClicked(courseId: String)
    }

    class CertificateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.certificateHeader)
        lateinit var header: ViewGroup

        @BindView(R.id.courseImage)
        lateinit var courseImage: ImageView

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.qualifiedCertificateContainer)
        lateinit var qualifiedCertificate: RelativeLayout

        @BindView(R.id.qualifiedCertificateButton)
        lateinit var qualifiedCertificateButton: Button

        @BindView(R.id.recordOfAchievementContainer)
        lateinit var recordOfAchievement: RelativeLayout

        @BindView(R.id.recordOfAchievementButton)
        lateinit var recordOfAchievementButton: Button

        @BindView(R.id.confirmationOfParticipationContainer)
        lateinit var confirmationOfParticipation: RelativeLayout

        @BindView(R.id.confirmationOfParticipationButton)
        lateinit var confirmationOfParticipationButton: Button

        init {
            ButterKnife.bind(this, view)
        }
    }

    companion object {

        val TAG = CertificateListAdapter::class.java.simpleName
    }
}
