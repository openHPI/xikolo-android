package de.xikolo.presenters.course_items;

import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CourseItemsPresenter implements Presenter<CourseItemsView> {

    public static final String TAG = CourseItemsPresenter.class.getSimpleName();

    private CourseItemsView view;

    private ItemManager itemManager;

    private Realm realm;

    private RealmResults itemListPromise;

    private String courseId;
    private String sectionId;

    // optional
    private String itemId;

    private int index = 0;

    private Course course;
    private Section section;
    private Item item;

    CourseItemsPresenter(String courseId, String sectionId, String itemId) {
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;
    }

    @Override
    public void onViewAttached(CourseItemsView v) {
        this.view = v;

        loadModels();

        view.setTitle(section.title);

        if (item != null) {
            index = item.position - 1;
        }

        Item firstItem = section.getItems().get(index);
        itemManager.updateItemVisited(firstItem.id);

        if (index == 0) {
            LanalyticsUtil.trackVisitedItem(firstItem.id, courseId, sectionId);
        }

        itemListPromise = itemManager.listItemsForSection(sectionId, realm, new RealmChangeListener<RealmResults<Item>>() {
            @Override
            public void onChange(RealmResults<Item> items) {
                view.setupView(items);
                view.setCurrentItem(index);
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

        if (itemListPromise != null) {
            itemListPromise.removeAllChangeListeners();
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
        view.startSectionDownload(course, section);
    }

    public boolean hasDownloadableContent() {
        for (Item item : section.getItems()) {
            if (Item.TYPE_VIDEO.equals(item.type)) {
                return true;
            }
        }
        return false;
    }

    private void loadModels() {
        if (course == null) {
            course = Course.get(courseId);
        }
        if (section == null) {
            section = Section.get(sectionId);
        }
        if (item == null && itemId != null) {
            item = Item.get(itemId);
        }
    }

}
