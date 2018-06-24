package de.xikolo.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import de.xikolo.R
import de.xikolo.storages.ApplicationPreferences
import java.io.File
import java.io.IOException

object StorageUtil {

    enum class StorageType {
        INTERNAL, SDCARD
    }

    /* Checks if external storage is available for read and write */
    @JvmStatic
    fun isStorageWritable(storage: File): Boolean {
        val state: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            state = Environment.getExternalStorageState(storage)
        else
            state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED
    }

    /* Checks if external storage is available to at least read */
    @JvmStatic
    fun isStorageReadable(storage: File): Boolean {
        val state: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            state = Environment.getExternalStorageState(storage)
        else
            state = Environment.getExternalStorageState()
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
        when (type) {
            StorageUtil.StorageType.SDCARD -> {
                val sdCard = getSdcardStorage(c)
                return sdCard ?: getInternalStorage(c)
            }
            StorageUtil.StorageType.INTERNAL -> return getInternalStorage(c)
            else -> return getInternalStorage(c)
        }
    }

    @JvmStatic
    fun getStoragePreference(c: Context): StorageType = toStorageType(c, ApplicationPreferences().storage!!)

    @JvmStatic
    fun migrateAsync(from: File, to: File, callback: StorageMigrationCallback) {
        Thread(Runnable {
            copiedFilesCount = 0
            if (from.exists()) {
                val totalFiles = FileUtil.folderFileNumber(from)
                move(from, to, callback)
                callback.onCompleted(copiedFilesCount == totalFiles)
            } else
                callback.onCompleted(false)
        }).start()
    }

    private var copiedFilesCount = 0
    private fun move(sourceFile: File, destFile: File, callback: StorageUtil.StorageMigrationCallback) {
        if (sourceFile.isDirectory) {
            for (file in sourceFile.listFiles()!!) {
                move(file, File(destFile.absolutePath + File.separator + file.name), callback)
            }
        } else {
            try {
                destFile.mkdirs()
                sourceFile.copyTo(destFile, true)
                copiedFilesCount++
                callback.onProgressChanged(copiedFilesCount)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        sourceFile.delete()
    }

    @JvmStatic
    fun buildMigrationMessage(c: Context, from: StorageType): String {
        var currentStorage = getStoragePreference(c)
        var current = c.getString(R.string.settings_title_storage_internal)
        if (currentStorage == StorageType.INTERNAL)
            current = c.getString(R.string.settings_title_storage_external)

        var destination = c.getString(R.string.settings_title_storage_external)
        if (from == StorageType.SDCARD)
            destination = c.getString(R.string.settings_title_storage_internal)

        return c.getString(R.string.dialog_storage_migration, current, destination)
    }

    @JvmStatic
    fun toStorageType(c: Context, s: String): StorageType = if (s == c.getString(R.string.settings_title_storage_external)) StorageType.SDCARD else StorageType.INTERNAL

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
