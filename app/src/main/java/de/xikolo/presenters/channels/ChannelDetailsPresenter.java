package de.xikolo.presenters.channels;

import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.ChannelManager;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmResults;

import static de.xikolo.jobs.base.RequestJobCallback.ErrorCode.NO_NETWORK;

public class ChannelDetailsPresenter extends LoadingStatePresenter<ChannelDetailsView> {

    public static final String TAG = ChannelDetailsPresenter.class.getSimpleName();

    private ChannelManager channelManager;

    private CourseManager courseManager;

    private Realm realm;

    private Channel channelPromise;

    private RealmResults coursesPromise;

    private String channelId;

    private Channel channel;

    ChannelDetailsPresenter(String channelId) {
        this.channelManager = new ChannelManager();
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.channelId = channelId;
    }

    @Override
    public void onRefresh() {
        requestChannel(true);
    }

    @Override
    public void onViewAttached(ChannelDetailsView v) {
        super.onViewAttached(v);

        if (channel == null) {
            requestChannel(false);
        }

        channelPromise = channelManager.getChannel(channelId, realm, c -> {
            channel = c;
            coursesPromise = courseManager.listCoursesForChannel(channelId, realm, courses -> {
                if(getView() != null)
                    getViewOrThrow().showCourses(courses);
                else if(coursesPromise != null)
                    coursesPromise.removeAllChangeListeners();
            });

            getViewOrThrow().showContent();
            getViewOrThrow().setupView(channel);//ToDO something is blocking the thread
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

    public void onEnrollButtonClicked(final String courseId) {
        getViewOrThrow().showBlockingProgress();

        courseManager.createEnrollment(courseId, new RequestJobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgress();
                    Course course = Course.get(courseId);
                    if (course.accessible) {
                        getView().enterCourse(courseId);
                    }
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    getView().hideProgress();
                    if (code == NO_NETWORK) {
                        getView().showNetworkRequiredMessage();
                    } else if (code == ErrorCode.NO_AUTH) {
                        getView().showLoginRequiredMessage();
                        getView().openLogin();
                    }
                }
            }
        });
    }

    public void onCourseEnterButtonClicked(String courseId) {
        if (!UserManager.isAuthorized()) {
            getViewOrThrow().showLoginRequiredMessage();
            getViewOrThrow().openLogin();
        } else {
            getViewOrThrow().enterCourse(courseId);
        }
    }

    public void onCourseDetailButtonClicked(String courseId) {
        getViewOrThrow().enterCourseDetails(courseId);
    }

    private void requestChannel(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        channelManager.requestChannelWithCourses(channelId, getDefaultJobCallback(userRequest));
    }

}
