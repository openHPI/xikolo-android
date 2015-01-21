package de.xikolo.controller.module;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import de.xikolo.R;
import de.xikolo.controller.helper.WebViewController;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.ItemText;
import de.xikolo.data.entities.Module;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class TextFragment extends PagerFragment<ItemText> {

    public static final String TAG = TextFragment.class.getSimpleName();

    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mProgress;

    private WebViewController mWebViewController;

    private ItemModel mItemModel;
    private Result<Item> mItemResult;

    public TextFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new TextFragment(), course, module, item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemModel = new ItemModel(getActivity(), jobManager, databaseHelper);
        mItemResult = new Result<Item>() {
            @Override
            protected void onSuccess(Item result, DataSource dataSource) {
                mRefreshLayout.setRefreshing(false);
                mItem = result;
                mWebViewController.request(Config.URI + Config.COURSES + mCourse.course_code + "/" + Config.ITEMS + mItem.id);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                mRefreshLayout.setRefreshing(false);
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                } else {
                    ToastUtil.show(getActivity(), R.string.error);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        mProgress = (ProgressBar) layout.findViewById(R.id.progress);

        mWebViewController = new WebViewController(getActivity(), mWebView, mRefreshLayout, mProgress);
        mWebViewController.setInAppLinksEnabled(true);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mRefreshLayout.setRefreshing(true);
            mItemModel.getItemDetail(mItemResult, mCourse, mModule, mItem, Item.TYPE_TEXT);
        }

        return layout;
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
