package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.models.Announcement;
import de.xikolo.presenters.base.LoadingStateView;

public interface NewsListView extends LoadingStateView {

    void openAnnouncement(String announcementId);

    void showAnnouncementList(List<Announcement> announcementList);

}
