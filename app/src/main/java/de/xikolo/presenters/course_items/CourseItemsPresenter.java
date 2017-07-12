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

    // optional
    private String itemId;

    private int index = 0;

    private Course course;
    private Section section;
    private Item firstItem;

    private List<Item> itemList;

    CourseItemsPresenter(String courseId, String sectionId, String itemId) {
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;

        loadModels();
    }

    @Override
    public void onViewAttached(CourseItemsView view) {
        super.onViewAttached(view);
        view.setTitle(section.title);

        if (firstItem != null) {
            index = firstItem.position - 1;
        }

        Item firstItem = section.getAccessibleItems().get(index);
        itemManager.updateItemVisited(firstItem.id);

        if (index == 0) {
            LanalyticsUtil.trackVisitedItem(firstItem.id, courseId, sectionId);
        }

        if (itemList == null) {
            itemList =  section.getAccessibleItems();
            getViewOrThrow().setupView(itemList);
            getViewOrThrow().setCurrentItem(index);
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void onItemSelected(String itemId) {
        itemManager.updateItemVisited(itemId);

        LanalyticsUtil.trackVisitedItem(itemId, courseId, sectionId);
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
        if (firstItem == null && itemId != null) {
            firstItem = Item.get(itemId);
        }
    }

}
