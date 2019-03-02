package de.xikolo.controllers.dates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.models.Course
import de.xikolo.models.CourseDate
import de.xikolo.utils.SectionList
import de.xikolo.utils.TimeUtil
import java.text.DateFormat
import java.util.*

class DateListAdapter(private val onDateClickListener: OnDateClickListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TAG: String = DateListAdapter::class.java.simpleName

        private const val ITEM_VIEW_TYPE_META = 0
        private const val ITEM_VIEW_TYPE_HEADER = 1
        private const val ITEM_VIEW_TYPE_ITEM = 2
    }

    private var dateList: SectionList<String, List<CourseDate>> = SectionList()
    private var todaysDateCount: Int = 0
    private var nextSevenDaysDateCount: Int = 0
    private var futureDateCount: Int = 0

    fun update(dateList: SectionList<String, List<CourseDate>>, todaysDateCount: Int, nextSevenDaysDateCount: Int, futureDateCount: Int) {
        this.dateList = dateList
        this.todaysDateCount = todaysDateCount
        this.nextSevenDaysDateCount = nextSevenDaysDateCount
        this.futureDateCount = futureDateCount
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dateList.size() + 1 // +1 for overview element
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0                   -> ITEM_VIEW_TYPE_META
            dateList.isHeader(position - 1) -> ITEM_VIEW_TYPE_HEADER
            else                            -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_META   -> {
                OverviewViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.container_date_overview, parent, false)
                )
            }
            ITEM_VIEW_TYPE_HEADER -> {
                HeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                )
            }
            else                  -> {
                CourseDateViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_date_list, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val actualPosition = position - 1 // -1 because of overview element

        if (holder is OverviewViewHolder) {
            holder.numberOfDatesToday.text = todaysDateCount.toString()
            holder.numberOfDatesWeek.text = nextSevenDaysDateCount.toString()
            holder.numberOfAllDates.text = futureDateCount.toString()
        } else if (holder is HeaderViewHolder) {
            val header = dateList.getItem(actualPosition) as String?
            if (header == null) {
                holder.header.visibility = View.GONE
            } else {
                holder.header.text = header
                holder.header.visibility = View.VISIBLE
            }
        } else {
            val courseDate = dateList.getItem(actualPosition) as CourseDate
            val viewHolder: CourseDateViewHolder = holder as CourseDateViewHolder

            viewHolder.textType.text = courseDate.getDisplayTypeString(App.getInstance())

            viewHolder.textCourse.text = Course.get(courseDate.courseId).title
            viewHolder.textCourse.setOnClickListener {
                onDateClickListener?.onCourseClicked(courseDate.courseId)
            }

            viewHolder.textDate.text = DateFormat.getDateTimeInstance(
                DateFormat.YEAR_FIELD or DateFormat.MONTH_FIELD or DateFormat.DATE_FIELD,
                DateFormat.SHORT,
                Locale.getDefault()
            ).format(courseDate.date)

            viewHolder.textDateTitle.text = courseDate.title

            courseDate.date?.time?.let {
                viewHolder.textTimeLeft.text = TimeUtil.getTimeLeftString(
                    it - Date().time,
                    App.getInstance()
                )
            }
        }
    }

    interface OnDateClickListener {

        fun onCourseClicked(courseId: String?)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.container)
        lateinit var container: ViewGroup

        @BindView(R.id.textHeader)
        lateinit var header: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }

    class OverviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {

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

    class CourseDateViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.textDate)
        lateinit var textDate: TextView

        @BindView(R.id.textType)
        lateinit var textType: TextView

        @BindView(R.id.textDateTitle)
        lateinit var textDateTitle: TextView

        @BindView(R.id.textTimeLeft)
        lateinit var textTimeLeft: TextView

        @BindView(R.id.textCourse)
        lateinit var textCourse: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }

}
