package de.xikolo.presenters.course;

import java.util.List;

import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.SectionManager;
import de.xikolo.models.Course;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class LearningsPresenter extends LoadingStatePresenter<LearningsView> {

    public static final String TAG = LearningsPresenter.class.getSimpleName();

    private CourseManager courseManager;

    private SectionManager sectionManager;

    private Realm realm;

    private Course coursePromise;

    private RealmResults sectionsPromise;

    private List<Section> sectionList;

    private String courseId;

    LearningsPresenter(String courseId) {
        this.courseManager = new CourseManager();
        this.sectionManager = new SectionManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
    }

    @Override
    public void onViewAttached(LearningsView v) {
        super.onViewAttached(v);

        if (sectionList == null) {
            requestSectionListWithItems();
        }

        coursePromise = courseManager.getCourse(courseId, realm, new RealmChangeListener<Course>() {
            @Override
            public void onChange(Course course) {
                getViewOrThrow().setTitle(course.title);
            }
        });

        sectionsPromise = sectionManager.listSectionsForCourse(courseId, realm, new RealmChangeListener<RealmResults<Section>>() {
            @Override
            public void onChange(RealmResults<Section> sections) {
                sectionList = sections;
                getViewOrThrow().showContent();
                getViewOrThrow().setupSections(sections);
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (coursePromise != null) {
            coursePromise.removeAllChangeListeners();
        }

        if (sectionsPromise != null) {
            sectionsPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestSectionListWithItems();
    }

    public void onSectionClicked(String sectionId) {
        getViewOrThrow().startCourseItemsActivity(courseId, sectionId, 0);
    }

    public void onSectionDownloadClicked(String sectionId) {
        getViewOrThrow().startSectionDownload(Course.get(courseId), Section.get(sectionId));
    }

    public void onItemClicked(String sectionId, int position) {
        getViewOrThrow().startCourseItemsActivity(courseId, sectionId, position);
    }

    private void requestSectionListWithItems() {
        if (getView() != null) {
            getView().showProgress();
        }
        sectionManager.requestSectionListWithItems(courseId, new JobCallback() {
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
