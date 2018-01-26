package de.xikolo.controllers.course_items;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import butterknife.OnClick;
import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.webview.WebViewActivityAutoBundle;
import de.xikolo.models.Item;
import de.xikolo.models.RichText;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course_items.RichTextPresenter;
import de.xikolo.presenters.course_items.RichTextPresenterFactory;
import de.xikolo.presenters.course_items.RichTextView;
import de.xikolo.utils.MarkdownUtil;

public class RichTextFragment extends LoadingStatePresenterFragment<RichTextPresenter, RichTextView> implements RichTextView {

    public static final String TAG = RichTextFragment.class.getSimpleName();

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;

    @BindView(R.id.title) TextView title;
    @BindView(R.id.text) TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_richtext;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void setupView(Item item, RichText richText) {
        title.setText(item.title);
        MarkdownUtil.formatAndSet(richText.text, text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                presenter.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fallback_button)
    public void fallbackButtonClicked() {
        presenter.fallbackButtonClicked();
    }

    public void openAsWebView(String title, String url, boolean inAppLinksEnabled, boolean externalLinksEnabled) {
        Intent intent = WebViewActivityAutoBundle.builder(title, url)
                .inAppLinksEnabled(inAppLinksEnabled)
                .externalLinksEnabled(externalLinksEnabled)
                .build(getActivity());
        startActivity(intent);
    }

    @NonNull
    @Override
    protected PresenterFactory<RichTextPresenter> getPresenterFactory() {
        return new RichTextPresenterFactory(courseId, sectionId, itemId);
    }

}
