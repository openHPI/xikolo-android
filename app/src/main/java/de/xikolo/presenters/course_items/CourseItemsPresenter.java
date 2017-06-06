package de.xikolo.presenters.course_items;

import java.util.List;

import de.xikolo.managers.CourseManager;
import de.xikolo.managers.SectionManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.presenters.base.Presenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CourseItemsPresenter implements Presenter<CourseItemsView> {

    public static final String TAG = CourseItemsPresenter.class.getSimpleName();

    private CourseItemsView view;

    private SectionManager sectionManager;

    private Realm realm;

    private Section sectionPromise;

    private String sectionId;

    CourseItemsPresenter(String sectionId) {
        this.sectionManager = new SectionManager();
        this.realm = Realm.getDefaultInstance();
        this.sectionId = sectionId;
    }

    @Override
    public void onViewAttached(CourseItemsView v) {
        this.view = v;

        sectionPromise = sectionManager.getSection(sectionId, realm, new RealmChangeListener<Section>() {
            @Override
            public void onChange(Section section) {
                if (view != null) {
                    view.setTitle(section.title);
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.view = null;

        if (sectionPromise != null) {
            sectionPromise.removeAllChangeListeners();
        }
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public boolean hasDownloadableContent(String sectionId) {
        for (Item item : Section.get(sectionId).getItems()) {
            if (Item.TYPE_VIDEO.equals(item.type)) {
                return true;
            }
        }
        return false;
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
