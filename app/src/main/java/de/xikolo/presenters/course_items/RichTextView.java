package de.xikolo.presenters.course_items;

import de.xikolo.models.Item;
import de.xikolo.models.RichText;
import de.xikolo.presenters.base.LoadingStateView;

public interface RichTextView extends LoadingStateView {

    void setupView(Item item, RichText richText);

    void openAsWebView(String title, String url, boolean inAppLinksEnabled, boolean externalLinksEnabled);

}
