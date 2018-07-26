package de.xikolo.controllers.course

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import de.xikolo.R
import de.xikolo.models.Document
import java.util.*

class DocumentListAdapter : RecyclerView.Adapter<DocumentListAdapter.DocumentViewHolder>() {

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
    }

    class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @BindView(R.id.title)
        lateinit var title: TextView

        @BindView(R.id.description)
        lateinit var description: TextView

        init {
            ButterKnife.bind(this, view)
        }

    }

}
