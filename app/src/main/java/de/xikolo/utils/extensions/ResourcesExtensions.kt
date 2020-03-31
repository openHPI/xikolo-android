package de.xikolo.utils.extensions

import android.content.Context
import android.content.res.Resources
import androidx.annotation.AnyRes
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes

fun Resources.getString(name: String, defPackage: String): String {
    return getString(getIdentifier(name, "string", defPackage))
}

fun Resources.getBoolean(name: String, defPackage: String): Boolean {
    return getBoolean(getIdentifier(name, "bool", defPackage))
}

fun Resources.getStringArray(name: String, defPackage: String): Array<out String> {
    return getStringArray(getIdentifier(name, "array", defPackage))
}

fun <T : Context> T.getString(name: String): String {
    return resources.getString(name, packageName)
}

fun <T : Context> T.getBoolean(@BoolRes id: Int): Boolean {
    return resources.getBoolean(id)
}

fun <T : Context> T.getBoolean(name: String): Boolean {
    return resources.getBoolean(name, packageName)
}

fun <T : Context> T.getStringArray(@ArrayRes id: Int): Array<out String> {
    return resources.getStringArray(id)
}

fun <T : Context> T.getStringArray(name: String): Array<out String> {
    return resources.getStringArray(name, packageName)
}

@AnyRes
fun <T : Context> T.getResId(name: String, type: String): Int {
    return resources.getIdentifier(name, type, packageName)
}

fun <T : Context> T.resExists(name: String, type: String): Boolean {
    return getResId(name, type) != 0
}
