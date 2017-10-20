package de.xikolo.controllers.downloads;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.managers.DownloadManager;

public class DownloadsActivity extends BaseActivity {

    public static final String TAG = DownloadsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        setTitle(getString(R.string.title_section_downloads));

        String tag = "downloads";

        DownloadManager downloadManager = new DownloadManager(this);
        downloadManager.startDownload();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, DownloadsFragment.newInstance(), tag);
            transaction.commit();
        }
    }

}
