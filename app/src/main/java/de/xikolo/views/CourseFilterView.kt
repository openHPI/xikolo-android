package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import de.xikolo.R
import de.xikolo.config.Feature
import de.xikolo.models.dao.ChannelDao
import de.xikolo.models.dao.CourseDao
import de.xikolo.utils.LanguageUtil
import java.util.*

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

        val classifierKeyList = mutableListOf<String>()
        val classifierTitleList = mutableListOf<String>()

        var deviceLanguage = Locale.getDefault().language
        if (deviceLanguage == "zh") { // workaround for wrong chinese API locale
            deviceLanguage = "cn"
        }
        val languages = CourseDao.Unmanaged.languages()
            .sortedBy {
                LanguageUtil.toNativeName(it)
            }
            .toMutableList()
        if (languages.contains("en")) {
            languages.remove("en")
            languages.add(0, "en")
        }
        if (languages.contains(deviceLanguage)) {
            languages.remove(deviceLanguage)
            languages.add(0, deviceLanguage)
        }
        classifierKeyList.add(context.getString(R.string.course_filter_classifier_language))
        classifierTitleList.add(context.getString(R.string.course_filter_language))

        val channels = if (Feature.enabled("channels")) {
            classifierKeyList.add(context.getString(R.string.course_filter_classifier_channel))
            classifierTitleList.add(context.getString(R.string.course_filter_channel))

            ChannelDao.Unmanaged.all()
                .sortedBy {
                    it.position
                }
        } else listOf()

        classifierKeyList.addAll(context.resources.getStringArray(R.array.course_filter_classifier_keys))
        classifierTitleList.addAll(context.resources.getStringArray(R.array.course_filter_classifier_titles))

        classifierKeyList.forEachIndexed { index, classifierKey ->
            currentFilter[classifierKey] = defaultOption.first

            val options: List<Pair<String, String>> = when (classifierKey) {
                context.getString(R.string.course_filter_classifier_language) -> {
                    languages.map {
                        Pair(it, LanguageUtil.toNativeName(it))
                    }
                }
                context.getString(R.string.course_filter_classifier_channel)  -> {
                    channels.map {
                        Pair(it.id, it.title)
                    }
                }
                else                                                          -> {
                    CourseDao.Unmanaged.collectClassifier(classifierKey)
                        .sorted()
                        .map {
                            Pair(it, it)
                        }
                }
            }

            val classifierView = LayoutInflater.from(context).inflate(R.layout.view_course_filter_classifier, this, false) as CourseFilterClassifierView
            classifierView.setClassifier(
                classifierTitleList[index],
                listOf(defaultOption) + options
            )
            classifierView.onSelected = { optionKey ->
                currentFilter[classifierKey] = optionKey
                onFilterChangeListener?.invoke(currentFilter)
            }
            addView(classifierView)
        }
    }

    private fun setupView() {
        orientation = VERTICAL
    }
}
