package de.xikolo.presenters.channels;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.managers.ChannelManager;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.utils.SectionList;
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

            if (isViewAttached()) {
                getView().showContent();
                getView().setupView(channel);
            }
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
                getView().showCourseList(courseList);
            }
        });
    }

    @Override
    public void buildCourseList() {
        List<Course> subList;

        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            subList = courseManager.listFutureCoursesForChannel(realm, channel.id);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_future_courses),
                        subList);
            }

            subList = courseManager.listCurrentAndPastCoursesForChannel(realm, channel.id);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                );
            }
        } else {
            subList = courseManager.listCurrentAndFutureCoursesForChannel(realm, channel.id);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                        subList
                );
            }

            subList = courseManager.listPastCoursesForChannel(realm, channel.id);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                );
            }
        }
    }

}
