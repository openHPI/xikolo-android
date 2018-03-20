package de.xikolo.presenters.main;

public class CourseListFilterChannelPresenterFactory extends CourseListPresenterFactory {

    private String channelId;

    public CourseListFilterChannelPresenterFactory(String channelId){
        this.channelId = channelId;
    }

    @Override
    public CourseListPresenter create() {
        return new CourseListFilterChannelPresenter(channelId);
    }
}
