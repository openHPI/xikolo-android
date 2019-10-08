package de.xikolo.controllers.channels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.base.BaseCourseListAdapter
import de.xikolo.models.Course
import de.xikolo.utils.MarkdownUtil
import de.xikolo.utils.extensions.isBetween
import java.util.*

class ChannelCourseListAdapter(fragment: Fragment, onCourseButtonClickListener: OnCourseButtonClickListener) : BaseCourseListAdapter<String>(fragment, onCourseButtonClickListener) {

    companion object {
        val TAG: String = ChannelCourseListAdapter::class.java.simpleName

        const val CHANNEL_COLOR_UNSET = -1
    }

    private var channelColor = CHANNEL_COLOR_UNSET

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_META   -> DescriptionViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.content_channel_description, parent, false)
            )
            ITEM_VIEW_TYPE_HEADER -> createHeaderViewHolder(parent, viewType)
            else                  -> createCourseViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder      -> bindHeaderViewHolder(holder, position)
            is DescriptionViewHolder -> {
                val description = super.contentList.get(position) as String?
                if (description != null) {
                    MarkdownUtil.formatAndSet(description, holder.text)
                } else {
                    holder.text.visibility = View.GONE
                }
            }
            is CourseViewHolder      -> {
                val course = super.contentList.get(position) as Course

                holder.textDescription.text = course.shortAbstract
                holder.textDescription.visibility = View.VISIBLE

                if (Date().isBetween(course.startDate, course.endDate)) {
                    holder.textBanner.visibility = View.VISIBLE
                    holder.textBanner.text = App.instance.getText(R.string.banner_running)
                    holder.textBanner.setBackgroundColor(ContextCompat.getColor(App.instance, R.color.banner_green))
                } else {
                    holder.textBanner.visibility = View.GONE
                }

                if (channelColor != CHANNEL_COLOR_UNSET) {
                    holder.buttonCourseAction.setTextColor(channelColor)
                    holder.buttonCourseDetails.setTextColor(channelColor)
                }

                bindCourseViewHolder(holder, position)
            }
        }
    }

    fun setThemeColor(@ColorInt color: Int) {
        this.channelColor = color
    }

    class DescriptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.text)
        lateinit var text: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

}
