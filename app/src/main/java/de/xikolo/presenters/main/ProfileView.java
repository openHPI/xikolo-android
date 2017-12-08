package de.xikolo.presenters.main;

import de.xikolo.models.Profile;
import de.xikolo.models.User;
import de.xikolo.presenters.base.LoadingStateView;

public interface ProfileView extends LoadingStateView {

    void showEnrollmentCount(int count);

    void showUser(User user, Profile profile);

}
