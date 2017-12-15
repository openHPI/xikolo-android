package de.xikolo.presenters.course_items;

import java.util.List;

import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.Realm;

public class CourseItemsPresenter extends Presenter<CourseItemsView> {

    public static final String TAG = CourseItemsPresenter.class.getSimpleName();

    private ItemManager itemManager;

    private Realm realm;

    private String courseId;
    private String sectionId;

    private int index;
    private int lastTrackedIndex;

    private Course course;
    private Section section;

    private List<Item> itemList;

    CourseItemsPresenter(String courseId, String sectionId, int index) {
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.index = index;
        this.lastTrackedIndex = -1;

        loadModels();
    }

    @Override
    public void onViewAttached(CourseItemsView view) {
        super.onViewAttached(view);
        view.setTitle(section.title);

        if (itemList == null) {
            itemList = section.getAccessibleItems();
        }

        if (index == 0) {
            Item item = itemList.get(index);

            realm.beginTransaction();
            item.visited = true;
            realm.commitTransaction();

            onItemSelected(index);
        }

        getViewOrThrow().setupView(itemList);
        getViewOrThrow().setCurrentItem(index);
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void onItemSelected(int position) {
        index = position;
        Item item = itemList.get(position);
        itemManager.updateItemVisited(item.id);

        if (lastTrackedIndex != index) LanalyticsUtil.trackVisitedItem(item.id, courseId, sectionId, item.contentType);
        lastTrackedIndex = index;
    }

    public void onSectionDownloadClicked() {
        getViewOrThrow().startSectionDownload(course, section);
    }

    public boolean hasDownloadableContent() {
        return section.hasDownloadableContent();
    }

    private void loadModels() {
        if (course == null) {
            course = Course.get(courseId);
        }
        if (section == null) {
            section = Section.get(sectionId);
        }
    }

}
