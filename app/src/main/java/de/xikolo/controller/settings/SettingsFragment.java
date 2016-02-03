package de.xikolo.controller.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Calendar;

import de.greenrobot.event.EventBus;
import de.xikolo.BuildConfig;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.LoginActivity;
import de.xikolo.controller.dialogs.ContributorsDialog;
import de.xikolo.controller.dialogs.LicensesDialog;
import de.xikolo.model.UserModel;
import de.xikolo.model.events.LoginEvent;
import de.xikolo.model.events.LogoutEvent;
import de.xikolo.view.SettingsDividerItemDecoration;

public class SettingsFragment extends PreferenceFragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();
    private Preference login_out;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static PreferenceFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        Preference copyright = findPreference("copyright");
        copyright.setTitle(String.format(String.valueOf(copyright.getTitle()), Calendar.getInstance().get(Calendar.YEAR)));
        copyright.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                switch (BuildConfig.buildFlavor) {
                    case OPEN_SAP:
                        openUrl("http://www.sap.com/corporate-en/about/legal/copyright/index.html");
                        break;
                    case OPEN_UNE:
                        openUrl("http://www.guofudata.com");
                        break;
                    default:
                        openUrl("https://hpi.de");
                }

                return true;
            }
        });

        Preference imprint = findPreference("imprint");
        imprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                switch (BuildConfig.buildFlavor) {
                    case OPEN_SAP:
                        openUrl("http://www.sap.com/corporate-en/about/legal/impressum.html");
                        break;
                    case MOOC_HOUSE:
                        openUrl("https://mooc.house/pages/imprint");
                        break;
                    case OPEN_UNE:
                        openUrl("https://openune.cn/pages/imprint");
                        break;
                    default:
                        openUrl("https://open.hpi.de/pages/imprint");
                }

                return true;
            }
        });

        Preference privacy = findPreference("privacy");
        privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                switch (BuildConfig.buildFlavor) {
                    case OPEN_SAP:
                        openUrl("http://www.sap.com/corporate-en/about/legal/privacy.html");
                        break;
                    case MOOC_HOUSE:
                        openUrl("https://mooc.house/pages/privacy");
                        break;
                    case OPEN_UNE:
                        openUrl("https://openune.cn/pages/privacy");
                        break;
                    default:
                        openUrl("https://open.hpi.de/pages/privacy");
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

        Preference contributors = findPreference("open_source_contributors");
        contributors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ContributorsDialog dialog = new ContributorsDialog();
                dialog.show(getFragmentManager(), ContributorsDialog.TAG);
                return true;
            }
        });

        login_out = findPreference("login_out");
        if (UserModel.isLoggedIn(getActivity())) {
            buildLogoutView(login_out);
        } else {
            buildLoginView(login_out);
        }
    }

    private void buildLoginView(Preference pref) {
        if (pref != null) {
            pref.setTitle(getString(R.string.login));
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
    }

    private void buildLogoutView(Preference pref) {
        if (pref != null) {
            pref.setTitle(getString(R.string.logout));
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UserModel userModel = new UserModel(GlobalApplication.getInstance().getJobManager());
                    userModel.logout();
                    EventBus.getDefault().post(new LogoutEvent());
                    return true;
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((RecyclerView) view.findViewById(R.id.list)).addItemDecoration(
                new SettingsDividerItemDecoration(getActivity())
        );
    }


    public void onEventMainThread(LoginEvent event) {
        buildLogoutView(login_out);
    }

    public void onEventMainThread(LogoutEvent event) {
        buildLoginView(login_out);
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}
