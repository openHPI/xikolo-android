package de.xikolo.openhpi.controller.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class ContentFragment extends Fragment {

    private static final String STATE_DRAWER = "state_drawer";

    protected boolean mDrawerOpen;

    protected OnFragmentInteractionListener mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.mDrawerOpen = savedInstanceState.getBoolean(STATE_DRAWER);
        } else {
            this.mDrawerOpen = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_DRAWER, mDrawerOpen);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public void onNavigationDrawerOpened() {
        this.mDrawerOpen = true;
    }

    public void onNavigationDrawerClosed() {
        this.mDrawerOpen = false;
    }

    public interface OnFragmentInteractionListener {

        public void onFragmentAttached(int id);

    }

}
