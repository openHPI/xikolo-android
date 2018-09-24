package de.xikolo.presenters.section;

import de.xikolo.presenters.base.PresenterFactory;

public class VideoPreviewPresenterFactory implements PresenterFactory<VideoPreviewPresenter> {

    private final String courseId;

    private final String sectionId;

    private final String itemId;

    public VideoPreviewPresenterFactory(String courseId, String sectionId, String itemId) {
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;
    }

    @Override
    public VideoPreviewPresenter create() {
        return new VideoPreviewPresenter(courseId, sectionId, itemId);
    }

}
