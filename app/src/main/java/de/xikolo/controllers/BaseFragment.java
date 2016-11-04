package de.xikolo.controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.birbit.android.jobqueue.JobManager;
import com.google.android.gms.cast.framework.CastContext;

import de.xikolo.GlobalApplication;
import de.xikolo.utils.PlayServicesUtil;

public abstract class BaseFragment extends Fragment {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected CastContext castContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();

        if (PlayServicesUtil.checkPlayServices(getContext())) {
            castContext = CastContext.getSharedInstance(getActivity());
        }
    }

}
