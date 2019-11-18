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
import de.xikolo.models.TicketTopic
import de.xikolo.utils.MetaSectionList

typealias HelpdeskTopic = Triple<String, TicketTopic, String?>
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
        holder.topicTitle.text = topic.first

        holder.layout.setOnClickListener {
            onTopicClickedListener?.onTopicClicked(topic.first, topic.second, topic.third)
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

}
