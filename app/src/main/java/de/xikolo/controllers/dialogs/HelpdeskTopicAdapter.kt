package de.xikolo.controllers.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.controllers.base.BaseMetaRecyclerViewAdapter
import de.xikolo.models.Course
import de.xikolo.models.TicketTopic
import de.xikolo.models.dao.CourseDao
import de.xikolo.utils.MetaSectionList
import java.util.*

typealias HelpdeskTopic = Pair<TicketTopic, String?>
typealias HelpdeskTopicList = MetaSectionList<String, Any, List<HelpdeskTopic>>

class HelpdeskTopicAdapter(private val onTopicClickedListener: OnTopicClickedListener?) : BaseMetaRecyclerViewAdapter<Any, HelpdeskTopic>() {

    companion object {
        val TAG: String = HelpdeskTopicAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> createHeaderViewHolder(parent, viewType)
            else                  -> createTopicViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder      -> bindHeaderViewHolder(holder, position)
            is CourseTitleViewHolder -> bindTopicViewHolder(holder, position)
        }
    }

    interface OnTopicClickedListener {
        fun onTopicClicked(title: String, topic: TicketTopic, courseId: String?)
    }

    class CourseTitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.helpdeskTopic)
        lateinit var topicTitle: TextView

        @BindView(R.id.container)
        lateinit var layout: ViewGroup

        @BindView(R.id.smallerContainer)
        lateinit var optionalViewGroup: ViewGroup

        @BindView(R.id.courseYear)
        lateinit var topicYear: TextView

        @BindView(R.id.courseSlug)
        lateinit var topicSlug: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

    private fun createTopicViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return CourseTitleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_helpdesk_topic_list, parent, false)
        )
    }

    private fun bindTopicViewHolder(holder: CourseTitleViewHolder, position: Int) {

        val topic = contentList.get(position) as HelpdeskTopic
        if (topic.first == TicketTopic.COURSE) {
            val course = CourseDao.Unmanaged.find(topic.second)
            holder.topicTitle.text = course?.title
            holder.topicSlug.text = course?.slug
            holder.topicYear.text = getCourseYear(course).toString()
            holder.optionalViewGroup.visibility = View.VISIBLE
        } else {
            holder.topicTitle.text = topic.first.toString()
            holder.optionalViewGroup.visibility = View.GONE
        }

        holder.layout.setOnClickListener {
            onTopicClickedListener?.onTopicClicked(holder.topicTitle.text.toString(), topic.first, topic.second)
        }

    }

    override fun createHeaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_header_secondary, parent, false).apply {
                findViewById<TextView>(R.id.textHeader).apply {
                    //additional left padding to match the rest of layout
                    setPadding((paddingLeft * 1.5f).toInt(), paddingTop, paddingRight, paddingBottom)
                }
            }
        )
    }

    private fun getCourseYear(course: Course?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = course!!.startDate
        return calendar.get(Calendar.YEAR)
    }

}
