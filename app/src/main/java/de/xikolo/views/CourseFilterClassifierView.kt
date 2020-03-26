package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import de.xikolo.R

class CourseFilterClassifierView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayoutCompat(context, attrs, defStyle) {

    private val title: TextView by lazy {
        findViewById<TextView>(R.id.classifier_title)
    }

    private val spinner: Spinner by lazy {
        findViewById<Spinner>(R.id.classifier_spinner)
    }

    var onSelected: ((String) -> Unit)? = null

    fun setClassifier(classifierTitle: String, classifierEntries: List<Pair<String, String>>) {
        title.text = classifierTitle

        spinner.adapter = ArrayAdapter<String>(context, R.layout.view_spinner_item, classifierEntries.map {
            it.second
        }).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onSelected?.invoke(classifierEntries[position].first)
            }
        }
    }
}