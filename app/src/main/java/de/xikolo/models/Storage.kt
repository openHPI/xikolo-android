package de.xikolo.models

import android.os.Environment
import de.xikolo.services.DownloadService
import de.xikolo.utils.extensions.fileCount
import java.io.File
import java.io.IOException

class Storage(val file: File) {

    enum class Type {
        INTERNAL, SDCARD
    }

    val isWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState(file)
            return state == Environment.MEDIA_MOUNTED
        }

    // removes empty folder structures and temporary files as well as old item files
    fun clean(file: File = this.file) {
        if (file.isDirectory && file.listFiles() != null) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    clean(child)
                }
            }
            if (file.listFiles().isEmpty()) {
                file.delete()
            }
        } else {
            if ((file.extension.endsWith("tmp")
                    && (DownloadService.instance == null
                    || DownloadService.instance?.isDownloadingTempFile(file) == false))
                || file.name.endsWith("slides.pdf")
                || file.name.endsWith("transcript.pdf")
                || file.name.endsWith("video_hd.mp4")
                || file.name.endsWith("video_sd.mp4")
                || file.name.endsWith("audio.mp3")
            ) file.delete()
        }
    }

    // moves the contents of the folder 'from' to the folder 'to'
    fun migrateTo(to: Storage?, callback: MigrationCallback) {
        if (to == null) {
            callback.onCompleted(false)
            return
        }

        Thread(Runnable {
            if (file.exists() && file.listFiles() != null) {
                callback.onProgressChanged(0)
                val totalFiles = file.fileCount
                var copiedFiles = 0
                for (file in file.listFiles()) {
                    copiedFiles += move(file, File(to.file.absolutePath + File.separator + file.name), callback)
                }
                callback.onCompleted(copiedFiles == totalFiles)
            } else {
                callback.onCompleted(false)
            }
        }).start()
    }

    private fun move(sourceFile: File, destFile: File, callback: MigrationCallback): Int {
        var count = 0
        if (sourceFile.isDirectory && sourceFile.listFiles() != null) {
            for (file in sourceFile.listFiles()) {
                count += move(file, File(destFile.absolutePath + File.separator + file.name), callback)
                callback.onProgressChanged(count)
            }
        } else {
            try {
                destFile.mkdirs()
                sourceFile.copyTo(destFile, true)
                count++
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        sourceFile.delete()

        return count
    }

    interface MigrationCallback {

        fun onProgressChanged(count: Int)

        fun onCompleted(success: Boolean)
    }

}
