package de.xikolo.presenters.channels;

import java.util.ArrayList;

import de.xikolo.managers.ChannelManager;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Channel;
import de.xikolo.models.base.SectionList;
import de.xikolo.presenters.base.BaseCourseListPresenter;
import io.realm.Realm;
import io.realm.RealmResults;

public class ChannelDetailsPresenter extends BaseCourseListPresenter<ChannelDetailsView> {

    public static final String TAG = ChannelDetailsPresenter.class.getSimpleName();

    private ChannelManager channelManager;

    private Channel channelPromise;

    private RealmResults coursesPromise;

    private String channelId;

    private Channel channel;

    ChannelDetailsPresenter(String channelId) {
        this.channelManager = new ChannelManager();
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.channelId = channelId;
        this.courseList = new SectionList<>();
    }

    @Override
    public void onRefresh() {
        requestChannel(true);
    }

    public void onViewAttached(ChannelDetailsView v) {
        super.onViewAttached(v);

        if (channel == null) {
            requestChannel(false);
        }

        channelPromise = channelManager.getChannel(channelId, realm, c -> {
            channel = c;

            coursesPromise = getCoursesPromise();

            getViewOrThrow().showContent();
            getViewOrThrow().setupView(channel);
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (channelPromise != null) {
            channelPromise.removeAllChangeListeners();
        }

        if (coursesPromise != null) {
            coursesPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    private void requestChannel(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        channelManager.requestChannelWithCourses(channelId, getDefaultJobCallback(userRequest));
    }

    private RealmResults getCoursesPromise() {
        return courseManager.listCoursesForChannel(channelId, realm, courses -> {
            if (getView() != null && courses.size() > 0) {
                courseList.clear();
                courseList.add(channel.description, new ArrayList<>());
                buildCourseList();
                getViewOrThrow().showCourseList(courseList);
            }
        });
    }

}
