package de.xikolo.presenters.video;

import de.xikolo.presenters.base.PresenterFactory;

public class VideoPresenterFactory implements PresenterFactory<VideoPresenter> {

    private final String courseId;

    private final String sectionId;

    private final String itemId;

    private final String videoId;

    public VideoPresenterFactory(String courseId, String sectionId, String itemId, String videoId) {
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;
        this.videoId = videoId;
    }

    @Override
    public VideoPresenter create() {
        return new VideoPresenter(courseId, sectionId, itemId, videoId);
    }

}
