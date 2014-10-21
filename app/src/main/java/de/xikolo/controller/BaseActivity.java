package de.xikolo.controller;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.path.android.jobqueue.JobManager;

import de.xikolo.GlobalApplication;
import de.xikolo.R;

public abstract class BaseActivity extends ActionBarActivity {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected ActionBar actionBar;

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();

    }

    protected void setupActionBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
        }

        toolbar = tb;

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
//            actionBar.setLogo(R.drawable.ic_logo);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        globalApplication.startCookieSyncManager();
    }

    @Override
    protected void onPause() {
        super.onPause();

        globalApplication.syncCookieSyncManager();
        globalApplication.stopCookieSyncManager();
    }

    @Override
    protected void onStop() {
        super.onStop();

        globalApplication.flushHttpResponseCache();
    }

}
