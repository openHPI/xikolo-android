package de.xikolo.config;

public class FeatureToggle {

    public static boolean secondScreen() {
        return false;
//        return (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP)
//                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
