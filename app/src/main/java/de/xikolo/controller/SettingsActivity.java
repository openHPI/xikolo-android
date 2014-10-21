package de.xikolo.controller;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import de.xikolo.R;
import de.xikolo.controller.settings.SettingsFragment;

public class SettingsActivity extends BaseActivity {

    public static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();

        setTitle(getString(R.string.title_section_settings));

        String tag = "settings";

        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, SettingsFragment.newInstance(), tag);
            transaction.commit();
        }
    }

}
