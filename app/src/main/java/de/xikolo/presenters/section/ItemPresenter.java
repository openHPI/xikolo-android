package de.xikolo.presenters.section;

import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.presenters.base.LoadingStateView;
import io.realm.Realm;

public class ItemPresenter<V extends LoadingStateView> extends LoadingStatePresenter<V> {

    protected ItemManager itemManager;

    protected Realm realm;

    protected String courseId;
    protected String sectionId;
    protected String itemId;

    protected Course course;
    protected Section section;
    protected Item item;

    ItemPresenter(String courseId, String sectionId, String itemId) {
        this.itemManager = new ItemManager();
        this.realm = Realm.getDefaultInstance();
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.itemId = itemId;

        loadModels();
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    @Override
    public void onRefresh() {
        requestItem(true);
    }

    protected void loadModels() {
        if (course == null) {
            course = Course.get(courseId);
        }
        if (section == null) {
            section = Section.get(sectionId);
        }
        if (item == null) {
            item = Item.get(itemId);
        }
    }

    protected void requestItem(boolean userRequest) {
        if (getView() != null) {
            getView().showProgress();
        }
        itemManager.requestItemWithContent(itemId, getDefaultJobCallback(userRequest));
    }

}
