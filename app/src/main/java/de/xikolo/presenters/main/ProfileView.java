package de.xikolo.presenters.main;

import de.xikolo.models.Profile;

public interface ProfileView extends MainView {

    void showEnrollmentCount(int count);

    void showProfile(Profile profile);

}
