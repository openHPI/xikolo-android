package de.xikolo.util;

import android.os.Build;

import de.xikolo.BuildConfig;

public class FeatureToggle {

    public static boolean secondScreen() {
        return (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_HPI || BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
