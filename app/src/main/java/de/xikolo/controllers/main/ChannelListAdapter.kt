package de.xikolo.controllers.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.GlideApp
import de.xikolo.models.Channel
import de.xikolo.models.Course

class ChannelListAdapter(private val callback: OnChannelCardClickListener) : RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder>() {

    companion object {
        val TAG: String = ChannelListAdapter::class.java.simpleName
        const val PREVIEW_COURSES_COUNT = 7
    }

    private var channelList: MutableList<Channel> = mutableListOf()

    private val courseLists: MutableList<List<Course>> = mutableListOf(listOf())

    fun update(channelList: List<Channel>, courseLists: List<List<Course>>) {
        this.channelList.clear()
        this.channelList.addAll(channelList)

        this.courseLists.clear()
        this.courseLists.addAll(courseLists)

        this.notifyDataSetChanged()
    }

    fun clear() {
        channelList.clear()
        courseLists.clear()
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return channelList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelListAdapter.ChannelViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_channel_list,
                parent,
                false
            )
        return ChannelListAdapter.ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelListAdapter.ChannelViewHolder, position: Int) {
        val channel = channelList[position]

        val channelColor = channel.colorOrDefault

        holder.textTitle.text = channel.title
        holder.textTitle.setTextColor(channelColor)

        holder.textDescription.text = channel.description

        holder.buttonChannelCourses.setTextColor(channelColor)
        holder.buttonChannelCourses.setOnClickListener { callback.onChannelClicked(channel.id) }

        holder.layout.setOnClickListener { callback.onChannelClicked(channel.id) }

        if (channel.imageUrl != null) {
            GlideApp.with(App.instance).load(channel.imageUrl).into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }

        holder.scrollContainer.removeAllViews()

        val courseList = courseLists[position]
        val courseCount = Math.min(PREVIEW_COURSES_COUNT, courseList.size)
        for (i in 0 until courseCount) {
            val course = courseList[i]

            val listItem = LayoutInflater
                .from(App.instance)
                .inflate(
                    R.layout.item_channel_course_list,
                    holder.scrollContainer,
                    false
                )

            val textTitle = listItem.findViewById<TextView>(R.id.textTitle)
            textTitle.text = course.title

            val imageView = listItem.findViewById<ImageView>(R.id.imageView)
            GlideApp
                .with(App.instance)
                .load(course.imageUrl)
                .into(imageView)

            listItem.setOnClickListener { callback.onCourseClicked(course) }

            holder.scrollContainer.addView(listItem)
        }

        if (courseList.size > PREVIEW_COURSES_COUNT) {
            val showMoreCard = LayoutInflater
                .from(App.instance)
                .inflate(
                    R.layout.item_channel_course_list_more,
                    holder.scrollContainer,
                    false
                )

            val imageView = showMoreCard.findViewById<ImageView>(R.id.imageView)
            GlideApp
                .with(App.instance)
                .load(channel.imageUrl)
                .into(imageView)

            showMoreCard.setOnClickListener { callback.onMoreCoursesClicked(channel.id, courseCount) }

            val card = showMoreCard.findViewById<View>(R.id.card_view)
            val params = card.layoutParams
            params.width += App.instance.resources.getDimension(R.dimen.corner_radius).toInt()
            card.layoutParams = params

            holder.scrollContainer.addView(showMoreCard)
            holder.scrollContainer.setPadding(
                holder.scrollContainer.paddingLeft,
                holder.scrollContainer.paddingTop,
                0,
                holder.scrollContainer.paddingBottom
            )
        }
    }

    interface OnChannelCardClickListener {

        fun onChannelClicked(channelId: String)

        fun onCourseClicked(course: Course)

        fun onMoreCoursesClicked(channelId: String, scrollPosition: Int)
    }

    class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.container)
        lateinit var layout: ViewGroup

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.textDescription)
        lateinit var textDescription: TextView

        @BindView(R.id.imageView)
        lateinit var imageView: ImageView

        @BindView(R.id.button_channel_courses)
        lateinit var buttonChannelCourses: Button

        @BindView(R.id.scrollContainer)
        lateinit var scrollContainer: LinearLayout

        init {
            ButterKnife.bind(this, view)
        }
    }

}
