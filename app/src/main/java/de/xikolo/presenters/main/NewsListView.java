package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.models.Announcement;

public interface NewsListView extends MainView {

    void openAnnouncement(String announcementId);

    void showAnnouncementList(List<Announcement> announcementList);

}
