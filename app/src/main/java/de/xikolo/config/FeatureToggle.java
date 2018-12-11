package de.xikolo.config;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import de.xikolo.BuildConfig;

public class FeatureToggle {

    public static boolean recapMode() {
        return BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI;
    }

    public static boolean documents() {
        return BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO;
    }

    public static boolean secondScreen() {
        return BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP;
    }

    public static boolean tracking() {
        return BuildConfig.X_TYPE == BuildType.RELEASE
                && (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP
                || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO || BuildConfig.X_FLAVOR == BuildFlavor.MOOC_HOUSE);
    }

    public static boolean channels() {
        return BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO || BuildConfig.X_FLAVOR == BuildFlavor.MOOC_HOUSE;
    }

    public static boolean hlsVideo() {
        return Config.DEBUG;
    }

    public static boolean pictureInPicture(Context context) {
        return Build.VERSION.SDK_INT >= 26 && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }
}
