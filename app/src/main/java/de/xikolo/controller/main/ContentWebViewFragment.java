package de.xikolo.controller.main;

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
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.util.Config;

public class ContentWebViewFragment extends ContentFragment {

    public static final String TAG = ContentWebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "arg_url";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_INAPP_LINKS = "arg_inapp_links";
    private static final String ARG_EXTERNAL_LINKS = "arg_external_links";
    private static final String ARG_ID = "arg_id";

    private String mUrl;
    private String mTitle;
    private boolean mInAppLinksEnabled;
    private boolean mExternalLinksEnabled;
    private int id;

    private WebView mWebView;
    private ProgressBar mProgress;
    private SwipeRefreshLayout mRefreshLayout;

    private WebViewController mWebViewController;

    public ContentWebViewFragment() {
        // Required empty public constructor
    }

    public static ContentWebViewFragment newInstance(int id, String url, String title, boolean inAppLinksEnabled, boolean externalLinksEnabled) {
        ContentWebViewFragment fragment = new ContentWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_INAPP_LINKS, inAppLinksEnabled);
        args.putBoolean(ARG_EXTERNAL_LINKS, externalLinksEnabled);
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_URL);
            mTitle = getArguments().getString(ARG_TITLE);
            mInAppLinksEnabled = getArguments().getBoolean(ARG_INAPP_LINKS);
            mExternalLinksEnabled = getArguments().getBoolean(ARG_EXTERNAL_LINKS);
            id = getArguments().getInt(ARG_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        mProgress = (ProgressBar) layout.findViewById(R.id.progress);

        mWebViewController = new WebViewController(getActivity(), mWebView, mRefreshLayout, mProgress);
        mWebViewController.setInAppLinksEnabled(mInAppLinksEnabled);
        mWebViewController.setLoadExternalUrlEnabled(mExternalLinksEnabled);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mWebViewController.request(mUrl);
        }

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivityCallback.onFragmentAttached(id, mTitle);
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
        if (!mActivityCallback.isDrawerOpen())
            inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_refresh:
                mWebViewController.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
