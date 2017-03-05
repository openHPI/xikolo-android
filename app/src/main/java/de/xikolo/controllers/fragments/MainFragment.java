package de.xikolo.controllers.fragments;

import de.xikolo.presenters.LoadingStatePresenter;
import de.xikolo.presenters.LoadingStateView;

public abstract class MainFragment<P extends LoadingStatePresenter<V>, V extends LoadingStateView> extends LoadingStatePresenterFragment<P, V> {

    protected MainActivityCallback activityCallback;

    @Override
    public void onStart() {
        super.onStart();
        try {
            activityCallback = (MainActivityCallback) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement MainActivityCallback");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activityCallback = null;
    }

    public interface MainActivityCallback {

        void onFragmentAttached(int id, String title);

        boolean isDrawerOpen();

        void updateDrawer();

        void selectDrawerSection(int pos);

    }

}
