package de.xikolo.controller;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.path.android.jobqueue.JobManager;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.database.DatabaseHelper;

public abstract class BaseActivity extends ActionBarActivity {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected DatabaseHelper databaseHelper;

    protected ActionBar actionBar;

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();
        databaseHelper = globalApplication.getDatabaseHelper();

        databaseHelper.open();
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

    protected void setActionBarElevation(float elevation) {
        if (actionBar != null && Build.VERSION.SDK_INT >= 21) {
            actionBar.setElevation(elevation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        globalApplication.startCookieSyncManager();
        databaseHelper.open();
    }

    @Override
    protected void onPause() {
        super.onPause();

        globalApplication.syncCookieSyncManager();
        globalApplication.stopCookieSyncManager();
        databaseHelper.close();
    }

    @Override
    protected void onStop() {
        super.onStop();

        globalApplication.flushHttpResponseCache();
    }

}
