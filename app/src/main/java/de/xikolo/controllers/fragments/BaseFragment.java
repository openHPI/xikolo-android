package de.xikolo.controllers.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.cast.framework.CastContext;
import com.hannesdorfmann.fragmentargs.FragmentArgs;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.utils.PlayServicesUtil;
import icepick.Icepick;

public abstract class BaseFragment extends Fragment {

    protected CastContext castContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentArgs.inject(this);

        if (PlayServicesUtil.checkPlayServices(getContext())) {
            castContext = CastContext.getSharedInstance(getActivity());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

}
