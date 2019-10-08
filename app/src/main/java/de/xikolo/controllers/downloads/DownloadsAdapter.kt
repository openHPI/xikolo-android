package de.xikolo.controllers.downloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.App
import de.xikolo.R
import de.xikolo.utils.FileUtil
import de.xikolo.utils.MetaSectionList
import java.io.File

class DownloadsAdapter(private val callback: OnDeleteButtonClickedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TAG: String = DownloadsAdapter::class.java.simpleName
        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_ITEM = 1
    }

    private val sectionList: MetaSectionList<String, Any, List<FolderItem>> =
        MetaSectionList()

    fun addItem(header: String, folder: List<FolderItem>) {
        if (folder.isNotEmpty()) {
            sectionList.add(header, folder)
            notifyDataSetChanged()
        }
    }

    fun clear() {
        sectionList.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = sectionList.size

    override fun getItemViewType(position: Int): Int {
        return if (sectionList.isHeader(position)) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header_secondary, parent, false)
            view.isEnabled = false
            view.setOnClickListener(null)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_download_list, parent, false)
            view.isEnabled = false
            view.setOnClickListener(null)
            FolderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.title.text = sectionList.get(position) as String
        } else {
            val viewHolder = holder as FolderViewHolder

            val folderItem = sectionList.get(position) as FolderItem

            val context = App.instance

            val dir = File(folderItem.path)
            viewHolder.textTitle.text = folderItem.title.replace("_".toRegex(), " ")

            val numberOfFiles = FileUtil.folderFileNumber(dir).toLong()

            viewHolder.textButtonDelete.setOnClickListener {
                callback.onDeleteButtonClicked(
                    folderItem
                )
            }

            if (numberOfFiles > 0) {
                viewHolder.textSubTitle.text = numberOfFiles.toString() + " " + context.getString(R.string.files) + ": " + FileUtil.getFormattedFileSize(FileUtil.folderSize(dir))
                viewHolder.textButtonDelete.visibility = View.VISIBLE
            } else {
                viewHolder.textSubTitle.text = numberOfFiles.toString() + " " + context.getString(R.string.files)
                viewHolder.textButtonDelete.visibility = View.GONE
            }

            if (position == itemCount - 1 || sectionList.isHeader(position + 1)) {
                viewHolder.viewDivider.visibility = View.INVISIBLE
            } else {
                viewHolder.viewDivider.visibility = View.VISIBLE
            }
        }
    }

    class FolderItem(val title: String, val path: String)

    interface OnDeleteButtonClickedListener {

        fun onDeleteButtonClicked(item: FolderItem)

    }

    internal class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.textTitle)
        lateinit var textTitle: TextView

        @BindView(R.id.textSubTitle)
        lateinit var textSubTitle: TextView

        @BindView(R.id.buttonDelete)
        lateinit var textButtonDelete: TextView

        @BindView(R.id.divider)
        lateinit var viewDivider: View

        init {
            ButterKnife.bind(this, view)
        }

    }

    internal class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.textHeader)
        lateinit var title: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }

}
