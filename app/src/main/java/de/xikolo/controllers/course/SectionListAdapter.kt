package de.xikolo.controllers.course

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.models.Section
import de.xikolo.utils.DateUtil
import de.xikolo.utils.DisplayUtil
import de.xikolo.views.AutofitRecyclerView
import de.xikolo.views.SpaceItemDecoration
import java.text.DateFormat
import java.util.*

class SectionListAdapter(private val activity: FragmentActivity,
                         private val sectionClickListener: OnSectionClickListener,
                         private val itemClickListener: ItemListAdapter.OnItemClickListener)
    : RecyclerView.Adapter<SectionListAdapter.SectionViewHolder>() {

    companion object {
        val TAG: String = SectionListAdapter::class.java.simpleName
    }

    private var sections: List<Section> = arrayListOf()

    fun updateSections(sections: List<Section>) {
        this.sections = sections
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_section,
                parent,
                false
            )
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]

        holder.textTitle.text = section.title

        val itemAdapter = ItemListAdapter(section, itemClickListener)
        holder.recyclerView.adapter = itemAdapter
        holder.recyclerView.setHasFixedSize(false)
        holder.recyclerView.clearItemDecorations()
        holder.recyclerView.addItemDecoration(SpaceItemDecoration(
            activity.resources.getDimensionPixelSize(R.dimen.card_horizontal_margin) / 2,
            activity.resources.getDimensionPixelSize(R.dimen.card_vertical_margin) / 2,
            false,
            object : SpaceItemDecoration.RecyclerViewInfo {
                override fun isHeader(position: Int): Boolean {
                    return false
                }

                override val spanCount: Int
                    get() = holder.recyclerView.spanCount

                override val itemCount: Int
                    get() = itemAdapter.itemCount
            }
        ))
        ViewCompat.setNestedScrollingEnabled(holder.recyclerView, false)

        if (section.hasAccessibleItems()) {
            contentAvailable(section, holder)
            itemAdapter.updateItems(section.accessibleItems)
        } else {
            contentLocked(section, holder)
        }
    }

    private fun contentAvailable(section: Section, holder: SectionViewHolder) {
        holder.progressBar.visibility = View.GONE
        holder.viewModuleNotification.visibility = View.GONE
        holder.viewHeader.setBackgroundColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_bg))
        holder.textTitle.setTextColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_text))

        val outValue = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        holder.layout.foreground = ContextCompat.getDrawable(activity, outValue.resourceId)
        holder.layout.setOnClickListener { sectionClickListener.onSectionClicked(section.id) }

        if (section.hasDownloadableContent()) {
            holder.viewDownloadButton.visibility = View.VISIBLE
            holder.viewDownloadButton.setOnClickListener { sectionClickListener.onSectionDownloadClicked(section.id) }
        } else {
            holder.viewDownloadButton.visibility = View.GONE
        }
    }

    private fun contentLocked(section: Section, holder: SectionViewHolder) {
        holder.progressBar.visibility = View.GONE
        holder.viewModuleNotification.visibility = View.VISIBLE
        holder.viewHeader.setBackgroundColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_bg_locked))
        holder.textTitle.setTextColor(ContextCompat.getColor(activity, R.color.apptheme_section_header_text_locked))

        holder.layout.isClickable = false
        holder.layout.foreground = null
        holder.viewDownloadButton.visibility = View.GONE
        if (section.startDate != null && DateUtil.isFuture(section.startDate)) {
            val dateOut: DateFormat
            if (DisplayUtil.is7inchTablet(activity)) {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault())
            } else {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
            }

            holder.textModuleNotification.text = String.format(activity.getString(R.string.available_at),
                dateOut.format(section.startDate))
        } else {
            holder.textModuleNotification.text = activity.getString(R.string.module_notification_no_content)
        }
    }

    interface OnSectionClickListener {

        fun onSectionClicked(sectionId: String)

        fun onSectionDownloadClicked(sectionId: String)
    }

    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.container)
        lateinit var layout: FrameLayout

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.recyclerView)
        lateinit var recyclerView: AutofitRecyclerView

        @BindView(R.id.containerProgress)
        lateinit var progressBar: ProgressBar

        @BindView(R.id.header)
        lateinit var viewHeader: View

        @BindView(R.id.moduleNotificationContainer)
        lateinit var viewModuleNotification: View

        @BindView(R.id.moduleNotificationLabel)
        lateinit var textModuleNotification: TextView

        @BindView(R.id.downloadBtn)
        lateinit var viewDownloadButton: View

        init {
            ButterKnife.bind(this, view)
        }

    }

}
