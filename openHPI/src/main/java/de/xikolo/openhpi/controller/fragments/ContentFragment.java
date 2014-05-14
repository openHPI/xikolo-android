package de.xikolo.openhpi.controller.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;

public abstract class ContentFragment extends Fragment {

    protected OnFragmentInteractionListener mCallback;

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

    public interface OnFragmentInteractionListener {

        public void onFragmentAttached(int id);

        public boolean isDrawerOpen();

    }

}
