package de.xikolo.controllers.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.google.android.gms.cast.framework.CastContext;
import com.yatatsu.autobundle.AutoBundle;

import butterknife.ButterKnife;
import de.xikolo.utils.PlayServicesUtil;

public abstract class BaseFragment extends Fragment {

    protected CastContext castContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // restore
            AutoBundle.bind(this, savedInstanceState);
        } else {
            AutoBundle.bind(this);
        }

        if (PlayServicesUtil.checkPlayServices(getContext())) {
            castContext = CastContext.getSharedInstance(getActivity());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AutoBundle.pack(this, outState);
    }

}
