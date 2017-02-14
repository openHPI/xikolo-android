package de.xikolo.controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.birbit.android.jobqueue.JobManager;
import com.google.android.gms.cast.framework.CastContext;

import de.xikolo.GlobalApplication;
import de.xikolo.utils.PlayServicesUtil;
import io.realm.Realm;

public abstract class BaseFragment extends Fragment {

    protected GlobalApplication globalApplication;

    protected JobManager jobManager;

    protected CastContext castContext;

    protected Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalApplication = GlobalApplication.getInstance();
        jobManager = globalApplication.getJobManager();

        realm = Realm.getDefaultInstance();

        if (PlayServicesUtil.checkPlayServices(getContext())) {
            castContext = CastContext.getSharedInstance(getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        realm.close();
    }
}
