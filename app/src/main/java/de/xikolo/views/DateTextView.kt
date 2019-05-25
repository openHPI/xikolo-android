package de.xikolo.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import de.xikolo.utils.DateUtil
import java.util.*


class DateTextView : AppCompatTextView, View.OnClickListener {

    companion object {
        private val TAG = DateTextView::class.java.simpleName
    }

    private var startDate: Date? = null
    private var endDate: Date? = null

    constructor(context: Context) : super(context) {
        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setOnClickListener(this)
    }

    fun setDate(date: Date) {
        startDate = date
        endDate = null
    }

    fun setDateSpan(startDate: Date, endDate: Date) {
        this.startDate = startDate
        this.endDate = endDate
    }

    private val message: String
        get() {
            return if (startDate != null) {
                if (endDate != null) { // date span
                    "${DateUtil.formatLocal(startDate)} - ${DateUtil.formatLocal(endDate)}\n\n${DateUtil.formatUTC(startDate)} - ${DateUtil.formatUTC(endDate)}"
                } else { // simple date
                    "${DateUtil.formatLocal(startDate)}\n\n${DateUtil.formatUTC(startDate)}"
                }
            } else {
                ""
            }
        }

    override fun isClickable(): Boolean {
        return true
    }

    override fun onClick(v: View?) {
        AlertDialog.Builder(context)
            .setTitle("Date detail")
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
