package de.xikolo.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.path.android.jobqueue.JobManager;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.DatabaseHelper;

public abstract class BaseFragment extends Fragment {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected DatabaseHelper databaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();
        databaseHelper = globalApplication.getDatabaseHelper();
    }

}
