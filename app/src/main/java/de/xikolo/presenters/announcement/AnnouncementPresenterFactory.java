package de.xikolo.presenters.announcement;

import de.xikolo.presenters.base.PresenterFactory;

public class AnnouncementPresenterFactory implements PresenterFactory<AnnouncementPresenter> {

    private final String announcementId;

    private boolean global;

    public AnnouncementPresenterFactory(String announcementId, boolean global) {
        this.announcementId = announcementId;
        this.global = global;
    }

    @Override
    public AnnouncementPresenter create() {
        return new AnnouncementPresenter(announcementId, global);
    }

}
