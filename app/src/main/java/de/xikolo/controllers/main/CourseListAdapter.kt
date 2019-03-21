package de.xikolo.controllers.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.base.BaseCourseListAdapter
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.models.Course
import de.xikolo.models.DateOverview
import de.xikolo.utils.DateUtil
import de.xikolo.utils.TimeUtil
import java.util.*

class CourseListAdapter(fragment: Fragment, private val courseFilter: CourseListFilter, onCourseButtonClickListener: BaseCourseListAdapter.OnCourseButtonClickListener, private val onDateOverviewClickListener: OnDateOverviewClickListener) : BaseCourseListAdapter<DateOverview>(fragment, onCourseButtonClickListener) {

    companion object {
        val TAG: String = CourseListAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_META   -> DateOverviewViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.content_date_overview, parent, false)
            )
            ITEM_VIEW_TYPE_HEADER -> createHeaderViewHolder(parent, viewType)
            else                  -> createCourseViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder       -> bindHeaderViewHolder(holder, position)
            is DateOverviewViewHolder -> {
                val dateOverview = super.contentList.get(position) as DateOverview

                holder.container.setOnClickListener { onDateOverviewClickListener.onDateOverviewClicked() }

                dateOverview.nextDate?.let { nextDate ->
                    nextDate.date?.let {
                        holder.textNextDate.text = String.format(
                            App.getInstance().getString(R.string.course_date_next),
                            TimeUtil.getTimeLeftString(
                                it.time - Date().time,
                                App.getInstance()
                            )
                        )
                    }
                    holder.textNextCourse.text = Course.get(nextDate.courseId).title
                    holder.titleOfNextDate.text = nextDate.title
                    holder.nextDateContainer.visibility = View.VISIBLE
                } ?: run {
                    holder.nextDateContainer.visibility = View.GONE
                }

                holder.numberOfDatesToday.text = dateOverview.todaysDateCount.toString()
                holder.numberOfDatesWeek.text = dateOverview.nextSevenDaysDateCount.toString()
                holder.numberOfAllDates.text = dateOverview.futureDateCount.toString()
            }
            is CourseViewHolder       -> {
                val course = super.contentList.get(position) as Course

                if (courseFilter == CourseListFilter.ALL) {
                    holder.textDescription.text = course.shortAbstract
                    holder.textDescription.visibility = View.VISIBLE

                    if (DateUtil.nowIsBetween(course.startDate, course.endDate)) {
                        holder.textBanner.visibility = View.VISIBLE
                        holder.textBanner.text = App.getInstance().getText(R.string.banner_running)
                        holder.textBanner.setBackgroundColor(ContextCompat.getColor(App.getInstance(), R.color.banner_green))
                    } else {
                        holder.textBanner.visibility = View.GONE
                    }
                } else {
                    holder.textDescription.visibility = View.GONE
                    when {
                        DateUtil.nowIsBetween(course.startDate, course.endDate) -> {
                            holder.textBanner.visibility = View.VISIBLE
                            holder.textBanner.text = App.getInstance().getText(R.string.banner_running)
                            holder.textBanner.setBackgroundColor(ContextCompat.getColor(App.getInstance(), R.color.banner_green))
                        }
                        DateUtil.isPast(course.endDate)                         -> {
                            holder.textBanner.visibility = View.VISIBLE
                            holder.textBanner.text = App.getInstance().getText(R.string.banner_self_paced)
                            holder.textBanner.setBackgroundColor(ContextCompat.getColor(App.getInstance(), R.color.banner_yellow))
                        }
                        else                                                    -> holder.textBanner.visibility = View.GONE
                    }
                }

                bindCourseViewHolder(holder, position)
            }
        }
    }

    interface OnDateOverviewClickListener {

        fun onDateOverviewClicked()
    }

    class DateOverviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.container)
        lateinit var container: View

        @BindView(R.id.nextDateContainer)
        lateinit var nextDateContainer: ViewGroup

        @BindView(R.id.textNextDate)
        lateinit var textNextDate: TextView

        @BindView(R.id.textNextCourse)
        lateinit var textNextCourse: TextView

        @BindView(R.id.textTitleOfNextDate)
        lateinit var titleOfNextDate: TextView

        @BindView(R.id.textNumberOfDatesToday)
        lateinit var numberOfDatesToday: TextView

        @BindView(R.id.textNumberOfDatesWeek)
        lateinit var numberOfDatesWeek: TextView

        @BindView(R.id.textNumberOfAllDates)
        lateinit var numberOfAllDates: TextView

        init {
            ButterKnife.bind(this, view)
        }
    }

}
