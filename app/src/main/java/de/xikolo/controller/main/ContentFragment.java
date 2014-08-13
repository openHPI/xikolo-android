package de.xikolo.controller.main;

import android.app.Activity;
import android.support.v4.app.Fragment;

import de.xikolo.controller.BaseFragment;

public abstract class ContentFragment extends BaseFragment {

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

        public void attachFragment(Fragment fragment);

        public void onTopLevelFragmentAttached(int id, String title);

        public void onLowLevelFragmentAttached(int id, String title);

        public boolean isDrawerOpen();

        public void updateDrawer();

        public void toggleDrawer(int pos);

    }

}
