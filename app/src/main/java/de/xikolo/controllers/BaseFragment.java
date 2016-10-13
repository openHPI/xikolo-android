package de.xikolo.controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.path.android.jobqueue.JobManager;

import de.xikolo.GlobalApplication;

public abstract class BaseFragment extends Fragment {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected VideoCastManager videoCastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();

        videoCastManager = VideoCastManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();

        videoCastManager = VideoCastManager.getInstance();
    }
}
