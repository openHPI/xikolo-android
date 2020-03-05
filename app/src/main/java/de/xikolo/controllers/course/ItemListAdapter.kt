package de.xikolo.controllers.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.models.Item
import de.xikolo.models.Section

class ItemListAdapter(private val section: Section, private val listener: OnItemClickListener) : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    companion object {
        val TAG: String = ItemListAdapter::class.java.simpleName
    }

    private val items: MutableList<Item> = mutableListOf()

    fun updateItems(items: List<Item>) {
        this.items.clear()
        this.items.addAll(items)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section_item_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val context = App.instance

        val item = items[position]

        holder.textTitle.text = formatTitle(section.title, item.title)

        holder.textIcon.setText(item.iconRes)

        if (item.timeEffort > 0) {
            holder.duration.text = item.formatTimeEffort()
        } else {
            holder.duration.visibility = View.GONE
        }

        if (!item.visited) {
            holder.viewUnseenIndicator.visibility = View.VISIBLE
        } else {
            holder.viewUnseenIndicator.visibility = View.GONE
        }

        if (!item.accessible) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
            holder.layout.foreground = null
            holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.text_light))
            holder.textIcon.setTextColor(ContextCompat.getColor(context, R.color.text_light))
            holder.viewUnseenIndicator.visibility = View.GONE
            holder.layout.isEnabled = false
        } else {
            holder.layout.setOnClickListener { listener.onItemClicked(section.id, holder.adapterPosition) }
        }
    }

    interface OnItemClickListener {

        fun onItemClicked(sectionId: String, position: Int)
    }

    private fun formatTitle(moduleTitle: String, itemTitle: String): String {
        return if (itemTitle.length > moduleTitle.length + 2 &&
            itemTitle.startsWith(moduleTitle) &&
            itemTitle.substring(moduleTitle.length + 1, moduleTitle.length + 2) == " ") {
            itemTitle.substring(moduleTitle.length + 2)
        } else {
            itemTitle
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.textIcon)
        lateinit var textIcon: TextView

        @BindView(R.id.unseenIndicator)
        lateinit var viewUnseenIndicator: View

        @BindView(R.id.container)
        lateinit var layout: FrameLayout

        @BindView(R.id.itemDuration)
        lateinit var duration: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }
}
