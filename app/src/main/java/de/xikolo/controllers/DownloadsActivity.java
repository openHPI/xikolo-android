package de.xikolo.controllers;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import de.xikolo.R;
import de.xikolo.controllers.activities.BaseActivity;
import de.xikolo.controllers.downloads.DownloadsFragment;

public class DownloadsActivity extends BaseActivity {

    public static final String TAG = DownloadsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        setTitle(getString(R.string.title_section_downloads));

        String tag = "downloads";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, DownloadsFragment.newInstance(), tag);
            transaction.commit();
        }
    }

}
