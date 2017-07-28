package de.xikolo.presenters.course;

import java.util.List;

import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.CourseProgress;
import de.xikolo.models.SectionProgress;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ProgressPresenter extends LoadingStatePresenter<ProgressView> {

    public static final String TAG = ProgressPresenter.class.getSimpleName();

    private CourseManager courseManager;

    private Realm realm;

    private RealmResults spPromise;

    private List<SectionProgress> spList;

    private String courseId;

    ProgressPresenter(String courseId) {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
    }

    @Override
    public void onViewAttached(ProgressView v) {
        super.onViewAttached(v);

        if (spList == null) {
            requestCourseProgressWithSections();
        }

        spPromise = courseManager.listSectionProgressesForCourse(courseId, realm, new RealmChangeListener<RealmResults<SectionProgress>>() {
            @Override
            public void onChange(RealmResults<SectionProgress> sectionProgresses) {
                spList = sectionProgresses;
                CourseProgress cp = CourseProgress.get(courseId);

                if (getView() != null && cp != null && spList != null) {
                    getView().setupView(cp, spList);
                    getView().showContent();
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (spPromise != null) {
            spPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestCourseProgressWithSections();
    }

    private void requestCourseProgressWithSections() {
        if (getView() != null) {
            getView().showProgress();
        }
        courseManager.requestCourseProgressWithSections(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                if (getView() != null) {
                    getView().hideProgress();
                }
            }

            @Override
            public void onError(ErrorCode code) {
                if (getView() != null) {
                    switch (code) {
                        case NO_NETWORK:
                            getView().showNetworkRequiredMessage();
                            break;
                        case CANCEL:
                        case ERROR:
                            getView().showErrorMessage();
                            break;
                    }
                }
            }
        });
    }

}
