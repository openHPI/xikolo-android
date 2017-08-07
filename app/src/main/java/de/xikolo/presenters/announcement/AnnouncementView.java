package de.xikolo.presenters.announcement;

import de.xikolo.models.Announcement;
import de.xikolo.presenters.base.LoadingStateView;

public interface AnnouncementView extends LoadingStateView {

    void showAnnouncement(Announcement announcement);

}
