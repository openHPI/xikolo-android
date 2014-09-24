package de.xikolo.controller.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controller.settings.dialog.LicensesDialog;

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

        Preference build_version = findPreference("build_version");
        build_version.setSummary(build_version.getSummary()
                + " "
                + BuildConfig.VERSION_NAME);
//        try {
//            build_version.setSummary(build_version.getSummary()
//                    + " "
//                    + getActivity().getPackageManager()
//                    .getPackageInfo(getActivity().getPackageName(), 0)
//                    .versionName);
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "Package " + getActivity().getPackageName() + " not found", e);
//            build_version.setSummary(build_version.getSummary() + " 0");
//        }

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

}
