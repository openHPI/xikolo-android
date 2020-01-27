package de.xikolo.utils

import android.net.Uri
import androidx.core.content.FileProvider
import de.xikolo.App
import de.xikolo.BuildConfig
import java.io.File

class FileProviderUtil : FileProvider() {

    companion object {
        fun getUriForFile(file: File): Uri {
            return getUriForFile(App.instance, BuildConfig.APPLICATION_ID + ".fileprovider", file)
        }
    }

}
