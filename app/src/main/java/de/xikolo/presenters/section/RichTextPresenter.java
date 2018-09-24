package de.xikolo.presenters.section;

import de.xikolo.config.Config;
import de.xikolo.models.RichText;
import de.xikolo.utils.LanalyticsUtil;
import io.realm.RealmResults;

public class RichTextPresenter extends ItemPresenter<RichTextView> {

    public static final String TAG = VideoPreviewPresenter.class.getSimpleName();

    private RealmResults richTextPromise;
    private RichText richText;

    RichTextPresenter(String courseId, String sectionId, String itemId) {
        super(courseId, sectionId, itemId);
    }

    @Override
    public void onViewAttached(RichTextView v) {
        super.onViewAttached(v);

        if (richText == null) {
            requestItem(false);
        }

        richTextPromise = itemManager.getRichTextForItem(item.contentId, realm, (result) -> {
            if (result.size() > 0) {
                richText = realm.copyFromRealm(result.first());
                if (isViewAttached()) {
                    getView().showContent();
                    getView().setupView(item, richText);
                }
            }
        });
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();

        if (richTextPromise != null) {
            richTextPromise.removeAllChangeListeners();
        }
    }

    public void fallbackButtonClicked() {
        getViewOrThrow().openAsWebView(
                item.title,
                Config.HOST_URL + Config.COURSES + courseId + "/" + Config.ITEMS + itemId,
                false,
                false
        );

        LanalyticsUtil.trackRichTextFallback(itemId, courseId, sectionId);
    }

}
