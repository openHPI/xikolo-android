package de.xikolo.presenters.announcement;

import de.xikolo.presenters.base.PresenterFactory;

public class AnnouncementPresenterFactory implements PresenterFactory<AnnouncementPresenter> {

    private final String announcementId;

    public AnnouncementPresenterFactory(String announcementId) {
        this.announcementId = announcementId;
    }

    @Override
    public AnnouncementPresenter create() {
        return new AnnouncementPresenter(announcementId);
    }

}
