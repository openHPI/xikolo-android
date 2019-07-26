package de.xikolo.config

import android.content.pm.PackageManager
import android.os.Build
import de.xikolo.App
import de.xikolo.BuildConfig

object FeatureConfig {

    val RECAP_MODE = BuildConfig.X_FLAVOR === BuildFlavor.OPEN_HPI

    val DOCUMENTS = BuildConfig.X_FLAVOR === BuildFlavor.OPEN_WHO

    @JvmField
    val SECOND_SCREEN =
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_HPI ||
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_SAP

    val TRACKING =
        BuildConfig.X_TYPE === BuildType.RELEASE && (BuildConfig.X_FLAVOR === BuildFlavor.OPEN_HPI ||
                BuildConfig.X_FLAVOR === BuildFlavor.OPEN_SAP ||
                BuildConfig.X_FLAVOR === BuildFlavor.OPEN_WHO ||
                BuildConfig.X_FLAVOR === BuildFlavor.MOOC_HOUSE)

    @JvmField
    val CHANNELS =
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_SAP ||
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_WHO ||
        BuildConfig.X_FLAVOR === BuildFlavor.MOOC_HOUSE

    @JvmField
    val HLS_VIDEO = Config.DEBUG

    @JvmField
    val PIP =
        Build.VERSION.SDK_INT >= 26 && App.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    val SSO_LOGIN =
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_WHO ||
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_SAP ||
        BuildConfig.X_FLAVOR === BuildFlavor.OPEN_HPI

}
