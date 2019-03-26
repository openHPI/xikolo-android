package de.xikolo.controllers.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.dialogs.base.BaseDialogFragment
import de.xikolo.models.dao.SubtitleTrackDao
import de.xikolo.utils.LanguageUtil

class ChooseLanguageDialog : BaseDialogFragment() {

    companion object {
        @JvmField val TAG: String = ChooseLanguageDialog::class.java.simpleName
    }

    @AutoBundleField
    lateinit var videoId: String

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var items: Array<CharSequence?>? = null

        val subtitles = SubtitleTrackDao.Unmanaged.allForVideo(videoId)

        if (subtitles.isNotEmpty()) {
            items = arrayOfNulls(subtitles.size)
            for (i in subtitles.indices) {
                items[i] = LanguageUtil.languageForCode(context, subtitles[i].language)
            }
        }

        val builder = AlertDialog.Builder(activity!!, R.style.AppTheme_Dialog)
        builder.setTitle(R.string.action_language)
            .setSingleChoiceItems(items, -1) { _, which ->
                listener?.onItemClick(which)
                dialog.cancel()
            }
            .setCancelable(true)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    interface Listener {
        fun onItemClick(position: Int)
    }

}
