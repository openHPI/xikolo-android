package de.xikolo.presenters.channels;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.jobs.base.RequestJobCallback;
import de.xikolo.managers.ChannelManager;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
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

    private SectionList<String, List<Course>> courseList;

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

    @Override
    public void onViewAttached(ChannelDetailsView v) {
        super.onViewAttached(v);

        if (channel == null) {
            requestChannel(false);
        }

        channelPromise = channelManager.getChannel(channelId, realm, c -> {
            channel = c;

            coursesPromise = courseManager.listCoursesForChannel(channelId, realm, courses -> {
                if(getView() != null && courses.size() > 0) {
                    courseList.clear();

                    courseList.add(channel.description, new ArrayList<>());

                    List<Course> subList;
                    if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                        subList = courseManager.listFutureCoursesForChannel(realm, channelId);
                        if (subList.size() > 0) {
                            courseList.add(
                                    App.getInstance().getString(R.string.header_future_courses),
                                    subList
                            );
                        }
                        subList = courseManager.listCurrentAndPastCoursesForChannel(realm, channelId);
                        if (subList.size() > 0) {
                            courseList.add(App.getInstance().getString(
                                    R.string.header_self_paced_courses),
                                    subList
                            );
                        }
                    } else {
                        subList = courseManager.listCurrentAndFutureCoursesForChannel(realm, channelId);
                        if (subList.size() > 0) {
                            courseList.add(
                                    App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                                    subList
                            );
                        }
                        subList = courseManager.listPastCoursesForChannel(realm, channelId);
                        if (subList.size() > 0) {
                            courseList.add(
                                    App.getInstance().getString(R.string.header_self_paced_courses),
                                    subList
                            );
                        }
                    }

                    getViewOrThrow().showContent(courseList);
                }
                else if(coursesPromise != null)
                    coursesPromise.removeAllChangeListeners();
            });

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
