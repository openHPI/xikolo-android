package de.xikolo.config;

import android.os.Build;

import de.xikolo.BuildConfig;

public class FeatureToggle {

    public static boolean recapMode() {
        return BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI;
    }

    public static boolean secondScreen() {
        return (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean tracking() {
        return BuildConfig.X_TYPE == BuildType.RELEASE
                && (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP
                || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO || BuildConfig.X_FLAVOR == BuildFlavor.MOOC_HOUSE);
    }

    public static boolean HAS_CHANNELS = true;

    public static boolean channels(){
        return HAS_CHANNELS;
    }

}
