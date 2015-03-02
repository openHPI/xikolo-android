package de.xikolo.controller.main;

import android.app.Activity;
import android.support.v4.app.Fragment;

import de.xikolo.controller.BaseFragment;

public abstract class ContentFragment extends BaseFragment {

    protected OnFragmentInteractionListener mActivityCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivityCallback = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityCallback = null;
    }

    public interface OnFragmentInteractionListener {

        public void onFragmentAttached(int id, String title);

        public boolean isDrawerOpen();

        public void updateDrawer();

        public void selectDrawerSection(int pos);

    }

}
