@file:JvmName("ToastUtil")

package de.xikolo.utils.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun <T : Context> T.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun <T : Context> T.showToast(@StringRes stringId: Int) {
    Toast.makeText(this, this.getString(stringId), Toast.LENGTH_SHORT).show()
}

fun <T : Fragment> T.showToast(@StringRes stringId: Int) {
    this.activity?.showToast(stringId)
}
