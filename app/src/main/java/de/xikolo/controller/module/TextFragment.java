package de.xikolo.controller.module;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.xikolo.R;
import de.xikolo.controller.helper.WebViewController;
import de.xikolo.entities.Course;
import de.xikolo.entities.Item;
import de.xikolo.entities.ItemText;
import de.xikolo.entities.Module;
import de.xikolo.model.ItemModel;
import de.xikolo.model.OnModelResponseListener;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class TextFragment extends PagerFragment<ItemText> {

    public static final String TAG = TextFragment.class.getSimpleName();

    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;

    private WebViewController mWebViewController;

    private ItemModel mItemModel;

    public TextFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new TextFragment(), course, module, item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemModel = new ItemModel(getActivity(), jobManager);
        mItemModel.setRetrieveItemDetailListener(new OnModelResponseListener<Item>() {
            @Override
            public void onResponse(final Item response) {
                if (response != null) {
                    mItem = response;
                    mWebViewController.request(Config.URI + Config.COURSES + mCourse.course_code + "/" + Config.ITEMS + mItem.id);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);

        mWebViewController = new WebViewController(getActivity(), mWebView, mRefreshLayout);
        mWebViewController.setInAppLinksEnabled(true);

        mRefreshLayout.setColorSchemeResources(
                R.color.apptheme_second,
                R.color.apptheme_main,
                R.color.apptheme_second,
                R.color.apptheme_main);
        mRefreshLayout.setOnRefreshListener(mWebViewController);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            if (NetworkUtil.isOnline(getActivity())) {
                mRefreshLayout.setRefreshing(true);
                mItemModel.retrieveItemDetail(mCourse.id, mModule.id, mItem.id, Item.TYPE_TEXT, true);
            } else {
                mRefreshLayout.setRefreshing(false);
                NetworkUtil.showNoConnectionToast(getActivity());
            }
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
