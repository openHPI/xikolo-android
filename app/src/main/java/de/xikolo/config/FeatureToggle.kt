package de.xikolo.config

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

import de.xikolo.BuildConfig

object FeatureToggle {

    @JvmStatic
    fun recapMode() = BuildConfig.X_FLAVOR === BuildFlavor.OPEN_HPI

    @JvmStatic
    fun documents() = BuildConfig.X_FLAVOR === BuildFlavor.OPEN_WHO

    @JvmStatic
    fun secondScreen() =
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_HPI ||
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_SAP

    @JvmStatic
    fun tracking() =
        BuildConfig.X_TYPE === BuildType.RELEASE && (BuildConfig.X_FLAVOR === BuildFlavor.OPEN_HPI ||
                BuildConfig.X_FLAVOR === BuildFlavor.OPEN_SAP ||
                BuildConfig.X_FLAVOR === BuildFlavor.OPEN_WHO ||
                BuildConfig.X_FLAVOR === BuildFlavor.MOOC_HOUSE)

    @JvmStatic
    fun channels() =
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_SAP ||
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_WHO ||
        BuildConfig.X_FLAVOR === BuildFlavor.MOOC_HOUSE

    @JvmStatic
    fun hlsVideo() = Config.DEBUG

    @JvmStatic
    fun pictureInPicture(context: Context) =
        Build.VERSION.SDK_INT >= 26 && context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

}
