package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentActivity
import de.xikolo.R
import de.xikolo.controllers.dialogs.DateInfoDialog
import de.xikolo.utils.DateUtil
import java.util.*

class DateTextView : AppCompatTextView, View.OnClickListener {

    companion object {
        private val TAG = DateTextView::class.java.simpleName
    }

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var infoTitle: String = resources.getString(R.string.dialog_date_info_default_title)

    constructor(context: Context) : super(context) {
        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setOnClickListener(this)
        updateAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setOnClickListener(this)
        updateAttributes(context, attrs)
    }

    fun setDate(date: Date) {
        startDate = date
        endDate = null

        updateView()
    }

    fun setDateSpan(startDate: Date, endDate: Date) {
        this.startDate = startDate
        this.endDate = endDate

        updateView()
    }

    private fun updateAttributes(context: Context, attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.DateTextView)
        infoTitle = attributes.getString(R.styleable.DateTextView_infoTitle)
            ?: resources.getString(R.string.dialog_date_info_default_title)
        attributes.recycle()
    }

    private fun updateView() {
        if (valid) {
            isClickable = true
            setCompoundDrawablesWithIntrinsicBounds(
                compoundDrawables[0],
                compoundDrawables[1],
                compoundDrawables[2],
                resources.getDrawable(R.drawable.dotted_line, context.theme)
            )
        } else {
            isClickable = false
            setCompoundDrawables(null, null, null, null)
        }
    }

    private val localText: String
        get() {
            return if (startDate != null) {
                if (endDate != null) { // date span
                    "${DateUtil.formatLocal(startDate)} - \n${DateUtil.formatLocal(endDate)}"
                } else { // simple date
                    DateUtil.formatLocal(startDate)
                }
            } else {
                ""
            }
        }

    private val utcText: String
        get() {
            return if (startDate != null) {
                if (endDate != null) { // date span
                    "${DateUtil.formatUTC(startDate)} - \n${DateUtil.formatUTC(endDate)}"
                } else { // simple date
                    DateUtil.formatUTC(startDate)
                }
            } else {
                ""
            }
        }

    private val valid: Boolean
        get() {
            return localText.isNotEmpty() && utcText.isNotEmpty()
        }

    override fun onClick(v: View?) {
        (context as? FragmentActivity)?.let {
            DateInfoDialog(infoTitle, localText, utcText).show(it.supportFragmentManager, DateInfoDialog.TAG)
        }
    }

}
