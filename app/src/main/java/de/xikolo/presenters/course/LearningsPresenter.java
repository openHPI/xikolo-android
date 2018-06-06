package de.xikolo.presenters.course;

import java.util.List;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.LoadingStatePresenter;
import io.realm.Realm;
import io.realm.RealmResults;

public class LearningsPresenter extends LoadingStatePresenter<LearningsView> {

    public static final String TAG = LearningsPresenter.class.getSimpleName();

    private CourseManager courseManager;

    private ItemManager itemManager;

    private Realm realm;

    private Course coursePromise;

    private RealmResults itemsPromise;

    private List<Item> itemList;

    private String courseId;

    LearningsPresenter(String courseId) {
        this.courseManager = new CourseManager();
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
    }

    @Override
    public void onViewAttached(LearningsView v) {
        super.onViewAttached(v);

        if (itemList == null) {
            requestSectionListWithItems(false);
        }

        coursePromise = courseManager.getCourse(courseId, realm, (course) -> {
            if (isViewAttached()) getView().setTitle(course.title);
        });

        itemsPromise = itemManager.listAccessibleItemsForCourse(courseId, realm, (items) -> {
            if (items.size() > 0) {
                itemList = items;
                if (isViewAttached()) {
                    getView().showContent();
                    getView().setupSections(Section.listForCourse(courseId));
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (coursePromise != null) {
            coursePromise.removeAllChangeListeners();
        }

        if (itemsPromise != null) {
            itemsPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestSectionListWithItems(true);
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

    private void requestSectionListWithItems(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        itemManager.requestSectionListWithItems(courseId, getDefaultJobCallback(userRequest));
    }

}
