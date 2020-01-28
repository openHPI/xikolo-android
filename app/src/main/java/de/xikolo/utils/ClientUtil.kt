package de.xikolo.utils

import de.xikolo.App
import java.io.File
import java.util.*

object ClientUtil {

    private const val FILE_NAME = "INSTALLATION"

    var id: String? = null
        get() {
            if (field == null) {
                val installationFile = File(App.instance.filesDir, FILE_NAME)
                try {
                    if (!installationFile.exists()) {
                        installationFile.writeText(
                            UUID.randomUUID().toString()
                        )
                    }
                    field = installationFile.readText()
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
            return field
        }
}
