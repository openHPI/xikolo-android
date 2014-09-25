package de.xikolo.controller.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controller.settings.dialog.LicensesDialog;
import de.xikolo.util.BuildFlavor;

public class SettingsFragment extends PreferenceFragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static PreferenceFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        Preference copyright = findPreference("copyright");
        copyright.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (BuildConfig.buildFlavor == BuildFlavor.OPEN_HPI) {
                    openUrl("https://hpi.de");
                } else if (BuildConfig.buildFlavor == BuildFlavor.OPEN_SAP) {
                    openUrl("http://www.sap.com/corporate-en/about/legal/copyright/index.html");
                }
                return true;
            }
        });

        Preference imprint = findPreference("imprint");
        imprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (BuildConfig.buildFlavor == BuildFlavor.OPEN_HPI) {
                    openUrl("https://open.hpi.de/pages/imprint");
                } else if (BuildConfig.buildFlavor == BuildFlavor.OPEN_SAP) {
                    openUrl("http://www.sap.com/corporate-en/about/legal/impressum.html");
                }
                return true;
            }
        });

        Preference privacy = findPreference("privacy");
        privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (BuildConfig.buildFlavor == BuildFlavor.OPEN_HPI) {
                    openUrl("https://open.hpi.de/pages/privacy");
                } else if (BuildConfig.buildFlavor == BuildFlavor.OPEN_SAP) {
                    openUrl("http://www.sap.com/corporate-en/about/legal/privacy.html");
                }
                return true;
            }
        });

        Preference build_version = findPreference("build_version");
        build_version.setSummary(build_version.getSummary()
                + " "
                + BuildConfig.VERSION_NAME);

        Preference licenses = findPreference("open_source_licenses");
        licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LicensesDialog dialog = new LicensesDialog();
                dialog.show(getFragmentManager(), LicensesDialog.TAG);
                return true;
            }
        });
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
