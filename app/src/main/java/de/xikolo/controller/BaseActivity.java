package de.xikolo.controller;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.path.android.jobqueue.JobManager;

import de.xikolo.GlobalApplication;

public abstract class BaseActivity extends FragmentActivity {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();
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
