package de.xikolo.controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.birbit.android.jobqueue.JobManager;
import com.google.android.gms.cast.framework.CastContext;

import de.xikolo.GlobalApplication;

public abstract class BaseFragment extends Fragment {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected CastContext castContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();

        castContext = CastContext.getSharedInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        castContext = CastContext.getSharedInstance(getActivity());
    }
}
