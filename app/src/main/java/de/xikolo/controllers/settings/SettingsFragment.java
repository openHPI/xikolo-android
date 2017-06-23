package de.xikolo.controllers.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import de.psdev.licensesdialog.LicensesDialog;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.controllers.login.LoginActivity;
import de.xikolo.events.LoginEvent;
import de.xikolo.events.LogoutEvent;
import de.xikolo.managers.UserManager;
import de.xikolo.config.BuildFlavor;
import de.xikolo.config.Config;

public class SettingsFragment extends PreferenceFragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    private Preference loginOut;

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


        if (Build.VERSION.SDK_INT < 23) {
            PreferenceScreen screen = (PreferenceScreen) findPreference(getString(R.string.preference_screen));
            PreferenceCategory video = (PreferenceCategory) findPreference(getString(R.string.preference_category_video_playback_speed));
            Preference videoPlaybackSpeed = findPreference(getString(R.string.preference_video_playback_speed));
            video.removePreference(videoPlaybackSpeed);
            screen.removePreference(video);
        }

        Preference copyright = findPreference(getString(R.string.preference_copyright));
        copyright.setTitle(String.format(String.valueOf(copyright.getTitle()), Calendar.getInstance().get(Calendar.YEAR)));
        copyright.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUrl(Config.COPYRIGHT_URL);
                return true;
            }
        });

        Preference imprint = findPreference(getString(R.string.preference_imprint));
        imprint.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUrl(Config.IMPRINT_URL);
                return true;
            }
        });

        Preference privacy = findPreference(getString(R.string.preference_privacy));
        privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openUrl(Config.PRIVACY_URL);
                return true;
            }
        });

        Preference termsOfUse = findPreference(getString(R.string.preference_terms_of_use));
        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_SAP) {
            termsOfUse.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    openUrl(Config.TERMS_OF_USE_URL);
                    return true;
                }
            });
        } else {
            PreferenceCategory info = (PreferenceCategory) findPreference(getString(R.string.preference_category_info));
            info.removePreference(termsOfUse);
        }

        Preference buildVersion = findPreference(getString(R.string.preference_build_version));
        buildVersion.setSummary(buildVersion.getSummary()
                + " "
                + BuildConfig.VERSION_NAME);

        Preference licenses = findPreference(getString(R.string.preference_open_source_licenses));
        licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog.Builder(getActivity())
                        .setNotices(R.raw.notices)
                        .setTitle(R.string.settings_title_licenses)
                        .build()
                        .show();
                return true;
            }
        });

        loginOut = findPreference(getString(R.string.preference_login_out));
        if (UserManager.isAuthorized()) {
            buildLogoutView(loginOut);
        } else {
            buildLoginView(loginOut);
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
                    UserManager.logout();
                    return true;
                }
            });
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        buildLogoutView(loginOut);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutEvent(LogoutEvent event) {
        buildLoginView(loginOut);
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
