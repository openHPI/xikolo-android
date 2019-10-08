package de.xikolo.utils

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import de.xikolo.R
import de.xikolo.services.DownloadService
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.utils.extensions.fileCount
import java.io.File
import java.io.IOException

object StorageUtil {

    enum class StorageType {
        INTERNAL, SDCARD
    }

    /* Checks if external storage is available for read and write */
    @JvmStatic
    fun isStorageWritable(storage: File): Boolean {
        val state = Environment.getExternalStorageState(storage)
        return state == Environment.MEDIA_MOUNTED
    }

    /* Checks if external storage is available to at least read */
    @JvmStatic
    fun isStorageReadable(storage: File): Boolean {
        val state = Environment.getExternalStorageState(storage)
        return state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
    }

    @JvmStatic
    fun getStorages(c: Context): Array<File> = ContextCompat.getExternalFilesDirs(c, null)

    /* Returns the "internal" storage. */
    @JvmStatic
    fun getInternalStorage(c: Context): File = getStorages(c)[0]

    /* Returns the "SD Card" storage or null if not available. */
    @JvmStatic
    fun getSdcardStorage(c: Context): File? {
        val storages = getStorages(c)
        return if (storages.size > 1) storages[1] else null
    }

    @JvmStatic
    fun getStorage(c: Context): File {
        val type = getStoragePreference(c)
        return when (type) {
            StorageUtil.StorageType.SDCARD -> {
                val sdCard = getSdcardStorage(c)
                sdCard ?: getInternalStorage(c)
            }
            else -> getInternalStorage(c)
        }
    }

    @JvmStatic
    fun getStoragePreference(c: Context): StorageUtil.StorageType = toStorageType(c, ApplicationPreferences().storage!!)

    // moves the contents of the folder 'from' to the folder 'to'
    @JvmStatic
    fun migrateAsync(from: File?, to: File?, callback: StorageMigrationCallback) {
        if (from == null || to == null) {
            callback.onCompleted(false)
            return
        }

        Thread(Runnable {
            if (from.exists() && from.listFiles() != null) {
                callback.onProgressChanged(0)
                val totalFiles = from.fileCount
                var copiedFiles = 0
                for (file in from.listFiles()) {
                    copiedFiles += move(file, File(to.absolutePath + File.separator + file.name), callback)
                }
                callback.onCompleted(copiedFiles == totalFiles)
            } else {
                callback.onCompleted(false)
            }
        }).start()
    }

    private fun move(sourceFile: File, destFile: File, callback: StorageUtil.StorageMigrationCallback): Int {
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

    // removes empty folder structures and temporary files as well as old item files
    @JvmStatic
    fun cleanStorage(file: File) {
        if (file.isDirectory && file.listFiles() != null) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    cleanStorage(child)
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

    @JvmStatic
    fun buildMigrationMessage(c: Context, from: StorageUtil.StorageType): String {
        val currentStorage = getStoragePreference(c)
        var current = c.getString(R.string.settings_title_storage_internal)
        if (currentStorage == StorageType.INTERNAL)
            current = c.getString(R.string.settings_title_storage_external)

        var destination = c.getString(R.string.settings_title_storage_external)
        if (from == StorageType.SDCARD)
            destination = c.getString(R.string.settings_title_storage_internal)

        return c.getString(R.string.dialog_storage_migration, current, destination)
    }

    @JvmStatic
    fun toStorageType(c: Context, s: String):
        StorageType =
            if (s == c.getString(R.string.settings_title_storage_external))
                StorageType.SDCARD
            else
                StorageType.INTERNAL

    @JvmStatic
    fun buildWriteErrorMessage(c: Context): String {
        var storage = c.getString(R.string.settings_title_storage_internal)
        if (getStoragePreference(c) == StorageType.SDCARD)
            storage = c.getString(R.string.settings_title_storage_external)
        return c.getString(R.string.toast_no_external_write_access, storage)
    }

    interface StorageMigrationCallback {
        fun onProgressChanged(count: Int)
        fun onCompleted(success: Boolean)
    }

}
