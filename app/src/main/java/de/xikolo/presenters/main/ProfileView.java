package de.xikolo.presenters.main;

import de.xikolo.models.Profile;
import de.xikolo.models.User;

public interface ProfileView extends MainView {

    void showEnrollmentCount(int count);

    void showUser(User user, Profile profile);

}
