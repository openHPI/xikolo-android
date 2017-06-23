package de.xikolo.presenters.course;

import java.util.List;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.SectionManager;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class LearningsPresenter implements LoadingStatePresenter<LearningsView> {

    public static final String TAG = LearningsPresenter.class.getSimpleName();

    private LearningsView view;

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
        this.view = v;

        coursePromise = courseManager.getCourse(courseId, realm, new RealmChangeListener<Course>() {
            @Override
            public void onChange(Course course) {
                if (view != null) {
                    view.setTitle(course.title);
                }
            }
        });

        sectionsPromise = sectionManager.listSectionsForCourse(courseId, realm, new RealmChangeListener<RealmResults<Section>>() {
            @Override
            public void onChange(RealmResults<Section> sections) {
                if (view != null) {
                    sectionList = sections;
                    view.setupSections(sections);
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

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

    public void onCreate() {
        requestSectionListWithItems();
    }

    public void onSectionClicked(String sectionId) {
        view.startCourseItemsActivity(courseId, sectionId, null);
    }

    public void onSectionDownloadClicked(String sectionId) {
        view.startSectionDownload(Course.get(courseId), Section.get(sectionId));
    }

    public void onItemClicked(String sectionId, String itemId) {
        view.startCourseItemsActivity(courseId, sectionId, itemId);
    }

    private void requestSectionListWithItems() {
        if (sectionList == null || sectionList.size() == 0) {
            view.showProgressMessage();
        } else {
            view.showRefreshProgress();
        }
        sectionManager.requestSectionListWithItems(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideAnyProgress();
            }

            @Override
            public void onError(ErrorCode code) {
                switch (code) {
                    case NO_NETWORK:
                        if (sectionList == null || sectionList.size() == 0) {
                            view.showNetworkRequiredMessage();
                        } else {
                            view.showNetworkRequiredToast();
                        }
                        break;
                    case CANCEL:
                    case ERROR:
                        view.showErrorToast();
                        break;
                }
            }
        });
    }

}
