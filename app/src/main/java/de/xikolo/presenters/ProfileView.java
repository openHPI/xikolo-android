package de.xikolo.presenters;

import de.xikolo.models.Profile;

public interface ProfileView extends MainView {

    void showEnrollmentCount(int count);

    void showProfile(Profile profile);

}
