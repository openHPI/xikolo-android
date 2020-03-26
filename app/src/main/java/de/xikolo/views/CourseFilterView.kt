package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import de.xikolo.R
import de.xikolo.models.dao.ChannelDao

class CourseFilterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayoutCompat(context, attrs, defStyle) {

    companion object {
        private val TAG = CourseFilterView::class.java.simpleName
    }

    var currentFilter = mutableMapOf<String, String>()

    var onFilterChangeListener: ((Map<String, String>) -> Unit)? = null

    val hasActiveFilter: Boolean
        get() = currentFilter.filter { it.key != defaultOption.first }.isNotEmpty()

    private val defaultOption = Pair(
        context.getString(R.string.course_filter_classifier_all),
        context.getString(R.string.course_filter_all)
    )

    init {
        setupView()
        clear()
        update()
    }

    fun clear() {
        currentFilter = mutableMapOf<String, String>()
        onFilterChangeListener = null
    }

    fun update() {
        removeAllViews()
        val classifierKeyList = context.resources.getStringArray(R.array.course_filter_classifier_keys)
        val classifierTitlesList = context.resources.getStringArray(R.array.course_filter_classifier_titles)
        classifierKeyList.forEachIndexed { index, classifierKey ->
            currentFilter[classifierKey] = defaultOption.first

            val options: List<Pair<String, String>> = when (classifierKey) {
                context.getString(R.string.course_filter_classifier_channel) -> {
                    ChannelDao.Unmanaged.all()
                        .map {
                            Pair<String, String>(it.id, it.title)
                        }
                }
                else                                                         -> {
                    val keys = getClassifierResourceKeys(classifierKey)
                    val titles = getClassifierResourceTitles(classifierKey)
                    mutableListOf<Pair<String, String>>()
                        .apply {
                            keys.forEachIndexed { index, _ ->
                                add(Pair(keys[index], titles[index]))
                            }
                        }
                }
            }

            val classifierView = LayoutInflater.from(context).inflate(R.layout.view_course_filter_classifier, this, false) as CourseFilterClassifierView
            classifierView.setClassifier(
                classifierTitlesList[index],
                listOf(defaultOption) + options
            )
            classifierView.onSelected = { optionKey ->
                currentFilter[classifierKey] = optionKey
                onFilterChangeListener?.invoke(currentFilter)
            }
            addView(classifierView)
        }
    }

    private fun getClassifierResourceKeys(name: String): Array<String> {
        return context.resources.getStringArray(
            context.resources.getIdentifier("course_filter_classifier_${name}_keys", "array", context.packageName)
        )
    }

    private fun getClassifierResourceTitles(name: String): Array<String> {
        return try {
            context.resources.getStringArray(
                context.resources.getIdentifier("course_filter_classifier_${name}_titles", "array", context.packageName)
            )
        } catch (e: Exception) { // when the titles are the same as the keys
            getClassifierResourceKeys(name)
        }
    }

    private fun setupView() {
        orientation = VERTICAL
    }
}
