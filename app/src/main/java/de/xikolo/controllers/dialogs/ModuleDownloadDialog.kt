package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment

class ModuleDownloadDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = ModuleDownloadDialog::class.java.simpleName
    }

    var listener: Listener? = null

    @AutoBundleField(required = false)
    var hdVideo: Boolean = false

    @AutoBundleField(required = false)
    var sdVideo: Boolean = false

    @AutoBundleField(required = false)
    var slides: Boolean = false

    @AutoBundleField
    lateinit var moduleTitle: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = String.format(activity!!.getString(R.string.dialog_module_downloads_title), moduleTitle)

        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setTitle(title)
            .setMultiChoiceItems(
                R.array.dialog_module_downloads_array,
                null
            ) { _, which, isChecked ->
                when (which) {
                    0 -> hdVideo = isChecked
                    1 -> sdVideo = isChecked
                    2 -> slides = isChecked
                }
            }
            .setPositiveButton(R.string.download) { _, _ ->
                listener?.onDialogPositiveClick(this, hdVideo, sdVideo, slides)
            }
            .setNegativeButton(R.string.dialog_negative) { _, _ -> dialog.cancel() }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment, hdVideo: Boolean, sdVideo: Boolean, slides: Boolean)
    }

}
