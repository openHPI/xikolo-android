package de.xikolo.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import de.xikolo.R
import de.xikolo.storages.ApplicationPreferences
import java.io.File

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
    fun toStorageType(c: Context, s: String): StorageType = if (s == c.getString(R.string.settings_title_storage_external)) StorageType.SDCARD else StorageType.INTERNAL

    @JvmStatic
    fun buildWriteErrorMessage(c: Context): String {
        var storage = c.getString(R.string.settings_title_storage_internal)
        if (getStoragePreference(c) == StorageType.SDCARD)
            storage = c.getString(R.string.settings_title_storage_external)
        return c.getString(R.string.toast_no_external_write_access, storage)
    }
}
