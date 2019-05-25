package de.xikolo.controllers.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.managers.UserManager
import de.xikolo.models.Announcement
import java.text.DateFormat
import java.util.*

class AnnouncementListAdapter(private val announcementClickListener: (String) -> Unit, private val global: Boolean = true) : RecyclerView.Adapter<AnnouncementListAdapter.AnnouncementViewHolder>() {

    companion object {
        val TAG: String = AnnouncementListAdapter::class.java.simpleName
    }

    private var announcementList: List<Announcement> = listOf()

    fun update(announcementList: List<Announcement>) {
        this.announcementList = announcementList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return announcementList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_list, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcementList[position]

        holder.title.text = announcement.title

        val lineSeparator = System.getProperty("line.separator")

        if (announcement.text != null && lineSeparator != null) {
            holder.text.text = announcement.text.replace(lineSeparator.toRegex(), "")
        } else {
            holder.text.text = null
        }

        val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
        holder.date.text = dateFormat.format(announcement.publishedAt)

        val course = announcement.course
        if (course != null && global) {
            holder.course.text = course.title
            holder.course.visibility = View.VISIBLE
            holder.bullet.visibility = View.VISIBLE
        } else {
            holder.course.visibility = View.GONE
            holder.bullet.visibility = View.GONE
        }

        if (announcement.visited || !UserManager.isAuthorized) {
            holder.unseenIndicator.visibility = View.INVISIBLE
        } else {
            holder.unseenIndicator.visibility = View.VISIBLE
        }

        holder.layout.setOnClickListener { _ -> announcementClickListener(announcement.id) }
    }

    class AnnouncementViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.container)
        lateinit var layout: ViewGroup

        @BindView(R.id.title)
        lateinit var title: TextView

        @BindView(R.id.date)
        lateinit var date: TextView

        @BindView(R.id.bullet)
        lateinit var bullet: TextView

        @BindView(R.id.course)
        lateinit var course: TextView

        @BindView(R.id.text)
        lateinit var text: TextView

        @BindView(R.id.unseen_indicator)
        lateinit var unseenIndicator: View

        init {
            ButterKnife.bind(this, view)
        }

    }

}
