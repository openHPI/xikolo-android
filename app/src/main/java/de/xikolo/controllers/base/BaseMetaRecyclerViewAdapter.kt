package de.xikolo.controllers.base

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.utils.MetaSectionList

abstract class BaseMetaRecyclerViewAdapter<M, S> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TAG: String = BaseMetaRecyclerViewAdapter::class.java.simpleName

        const val ITEM_VIEW_TYPE_META = 0
        const val ITEM_VIEW_TYPE_HEADER = 1
        const val ITEM_VIEW_TYPE_ITEM = 2
    }

    protected var contentList: MetaSectionList<String, M, List<S>> = MetaSectionList()

    fun update(contentList: MetaSectionList<String, M, List<S>>) {
        this.contentList = contentList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            contentList.isMetaItem(position) -> ITEM_VIEW_TYPE_META
            contentList.isHeader(position)   -> ITEM_VIEW_TYPE_HEADER
            else                             -> ITEM_VIEW_TYPE_ITEM
        }
    }

    fun isMetaItem(position: Int): Boolean {
        return contentList.isMetaItem(position)
    }

    fun isHeader(position: Int): Boolean {
        return contentList.isHeader(position)
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    protected fun bindHeaderViewHolder(holder: HeaderViewHolder, position: Int) {
        val header = contentList.get(position) as String?
        if (header == null) {
            holder.header.visibility = View.GONE
        } else {
            holder.header.text = header
            holder.header.visibility = View.VISIBLE
        }
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

}
