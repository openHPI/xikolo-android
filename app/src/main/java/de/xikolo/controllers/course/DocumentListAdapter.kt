package de.xikolo.controllers.course

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.controllers.helper.DownloadViewHelper
import de.xikolo.models.Document
import de.xikolo.models.DownloadAsset
import java.util.*

class DocumentListAdapter(val activity: FragmentActivity) : RecyclerView.Adapter<DocumentListAdapter.DocumentViewHolder>() {

    companion object {
        val TAG: String = DocumentListAdapter::class.java.simpleName
    }

    var documentList: MutableList<Document> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return documentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentListAdapter.DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_documents_list, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentListAdapter.DocumentViewHolder, position: Int) {
        val document = documentList[position]

        holder.title.text = document.title
        holder.description.text = document.description

        holder.downloadsLayout.removeAllViews()
        document.localizations.forEach { l ->
            val downloadViewHelper = DownloadViewHelper(
                activity,
                DownloadAsset.Document(document, l),
                String.format(activity.getString(R.string.document_lang), l.language)
            )
            downloadViewHelper.textFileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
            holder.downloadsLayout.addView(downloadViewHelper.view)
        }
    }

    class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.title)
        lateinit var title: TextView

        @BindView(R.id.description)
        lateinit var description: TextView

        @BindView(R.id.downloads_layout)
        lateinit var downloadsLayout: LinearLayout

        init {
            ButterKnife.bind(this, view)
        }

    }

}
