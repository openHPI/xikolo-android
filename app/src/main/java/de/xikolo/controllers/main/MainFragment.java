package de.xikolo.controllers.main;

import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.presenters.main.MainView;

public abstract class MainFragment<P extends LoadingStatePresenter<V>, V extends MainView> extends LoadingStatePresenterFragment<P, V> implements MainView {

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

    @Override
    public void goToProfile() {
        activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
    }

    public interface MainActivityCallback {

        void onFragmentAttached(int id, String title);

        boolean isDrawerOpen();

        void updateDrawer();

        void selectDrawerSection(int pos);

    }

}