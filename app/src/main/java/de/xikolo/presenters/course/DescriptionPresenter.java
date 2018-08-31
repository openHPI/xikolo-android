package de.xikolo.presenters.course;

import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;

public class DescriptionPresenter extends LoadingStatePresenter<DescriptionView> {

    public static final String TAG = DescriptionPresenter.class.getSimpleName();

    private CourseManager courseManager;

    private Realm realm;

    private Course coursePromise;

    private String courseId;

    private Course course;

    DescriptionPresenter(String courseId) {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
    }

    @Override
    public void onRefresh() {
        requestCourse(true);
    }

    @Override
    public void onViewAttached(DescriptionView v) {
        super.onViewAttached(v);

        if (course == null) {
            requestCourse(false);
        }

        coursePromise = courseManager.getCourse(courseId, realm, c -> {
            course = c;
            if (getView() != null) {
                getViewOrThrow().showContent();
                getViewOrThrow().setupView(course);
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (coursePromise != null) {
            coursePromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    private void requestCourse(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        courseManager.requestCourse(courseId, getDefaultJobCallback(userRequest));
    }

}
