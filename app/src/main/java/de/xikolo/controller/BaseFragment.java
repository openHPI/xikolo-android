package de.xikolo.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.path.android.jobqueue.JobManager;

import de.xikolo.GlobalApplication;

public abstract class BaseFragment extends Fragment {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();
    }

}
