package de.xikolo.controller.module;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import de.xikolo.R;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.controller.helper.WebViewController;
import de.xikolo.data.entities.AssignmentItemDetail;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.ItemDetail;
import de.xikolo.data.entities.LtiItemDetail;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.TextItemDetail;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class WebItemFragment<T extends ItemDetail> extends PagerFragment<T> {

    public static final String TAG = WebItemFragment.class.getSimpleName();

    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;

    private NotificationController mNotificationController;

    private WebViewController mWebViewController;

    private ItemModel mItemModel;

    public WebItemFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        if (item.type.equals(Item.TYPE_TEXT)) {
            return PagerFragment.newInstance(new WebItemFragment<TextItemDetail>(), course, module, item);
        } else if (item.type.equals(Item.TYPE_VIDEO)) {
            return PagerFragment.newInstance(new WebItemFragment<VideoItemDetail>(), course, module, item);
        } else if (item.type.equals(Item.TYPE_SELFTEST)
                || item.type.equals(Item.TYPE_ASSIGNMENT)
                || item.type.equals(Item.TYPE_EXAM)) {
            return PagerFragment.newInstance(new WebItemFragment<AssignmentItemDetail>(), course, module, item);
        } else if (item.type.equals(Item.TYPE_LTI)) {
            return PagerFragment.newInstance(new WebItemFragment<LtiItemDetail>(), course, module, item);
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemModel = new ItemModel(getActivity(), jobManager, databaseHelper);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);

        mNotificationController = new NotificationController(layout);

        mWebViewController = new WebViewController(getActivity(), layout);

        if (mItem.type.equals(Item.TYPE_TEXT)
                || mItem.type.equals(Item.TYPE_VIDEO)
                || mItem.type.equals(Item.TYPE_LTI)) {
            mWebViewController.setInAppLinksEnabled(false);
            mWebViewController.setLoadExternalUrlEnabled(false);
        } else if (mItem.type.equals(Item.TYPE_SELFTEST)
                || mItem.type.equals(Item.TYPE_ASSIGNMENT)
                || mItem.type.equals(Item.TYPE_EXAM)) {
            mWebViewController.setInAppLinksEnabled(true);
            mWebViewController.setLoadExternalUrlEnabled(false);
        }

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            requestItemDetail(false);
        }

        return layout;
    }

    private void requestItemDetail(final boolean userRequest) {
        Result<Item> result = new Result<Item>() {
            @Override
            protected void onSuccess(Item result, DataSource dataSource) {
                mItem = result;
                mWebViewController.request(Config.URI + Config.COURSES + mCourse.course_code + "/" + Config.ITEMS + mItem.id, false);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                mRefreshLayout.setRefreshing(false);
                if (errorCode == ErrorCode.NO_NETWORK) {
                    mNotificationController.setTitle(R.string.notification_no_network);
                    mNotificationController.setSummary(R.string.notification_no_network_summary);
                    mNotificationController.setNotificationVisible(true);
                    mNotificationController.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestItemDetail(true);
                        }
                    });
                    if (userRequest) {
                        NetworkUtil.showNoConnectionToast(getActivity());
                    }
                } else {
                    ToastUtil.show(getActivity(), R.string.error);
                }
            }
        };

        mNotificationController.setProgressVisible(true);
        mItemModel.getItemDetail(result, mCourse, mModule, mItem, mItem.type);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mWebView != null) {
            mWebView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                mWebViewController.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void pageChanged() {

    }

    @Override
    public void pageScrolling(int state) {

    }

}
