package de.xikolo.controller.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
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
import de.xikolo.util.BuildFlavor;
import de.xikolo.util.Config;
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
        if (BuildConfig.buildFlavor == BuildFlavor.OPEN_SAP) {
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

        Preference build_version = findPreference(getString(R.string.preference_build_version));
        build_version.setSummary(build_version.getSummary()
                + " "
                + BuildConfig.VERSION_NAME);

        Preference licenses = findPreference(getString(R.string.preference_open_source_licenses));
        licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LicensesDialog dialog = new LicensesDialog();
                dialog.show(getFragmentManager(), LicensesDialog.TAG);
                return true;
            }
        });

        Preference contributors = findPreference(getString(R.string.preference_open_source_contributors));
        contributors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ContributorsDialog dialog = new ContributorsDialog();
                dialog.show(getFragmentManager(), ContributorsDialog.TAG);
                return true;
            }
        });

        login_out = findPreference(getString(R.string.preference_login_out));
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
