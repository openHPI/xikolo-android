package de.xikolo.config;

import de.xikolo.BuildConfig;

public class FeatureToggle {

    public static boolean secondScreen() {
        return false;
//        return (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP)
//                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean tracking() {
        return BuildConfig.X_TYPE == BuildType.RELEASE
                && (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP
                || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO || BuildConfig.X_FLAVOR == BuildFlavor.MOOC_HOUSE);
    }

}
