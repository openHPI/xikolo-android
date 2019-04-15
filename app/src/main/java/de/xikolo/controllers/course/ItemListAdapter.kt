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
import de.xikolo.utils.ItemTitleUtil

class ItemListAdapter(private val section: Section, private val listener: OnItemClickListener) : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    companion object {
        val TAG: String = ItemListAdapter::class.java.simpleName
    }

    private var items: List<Item> = arrayListOf()

    fun updateItems(items: List<Item>) {
        this.items = items
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val context = App.instance

        val item = items[position]

        holder.textTitle.text = ItemTitleUtil.format(section.title, item.title)

        holder.textIcon.setText(item.iconRes)

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

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.textIcon)
        lateinit var textIcon: TextView

        @BindView(R.id.unseenIndicator)
        lateinit var viewUnseenIndicator: View

        @BindView(R.id.container)
        lateinit var layout: FrameLayout

        init {
            ButterKnife.bind(this, view)
        }

    }

}
