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

public class WebViewFragment extends ContentFragment {

    public static final String TAG = WebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "arg_url";
    private static final String ARG_TOP_LEVEL_CONTENT = "arg_top_level_content";
    private static final String ARG_TITLE = "arg_title";

    private String mUrl;
    private String mTitle;
    private boolean isTopLevelContent;

    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;

    private WebViewController mWebViewController;

    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment newInstance(String url, boolean topLevelContent, String title) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putBoolean(ARG_TOP_LEVEL_CONTENT, topLevelContent);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_URL);
            isTopLevelContent = getArguments().getBoolean(ARG_TOP_LEVEL_CONTENT);
            mTitle = getArguments().getString(ARG_TITLE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isTopLevelContent && mUrl.contains(Config.NEWS)) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_NEWS, getString(R.string.title_section_news));
        } else if (!isTopLevelContent && mTitle != null) {
            mCallback.onLowLevelFragmentAttached(NavigationAdapter.NAV_ID_LOW_LEVEL_CONTENT, mTitle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);

        mWebViewController = new WebViewController(getActivity(), mWebView, mRefreshLayout);

        mRefreshLayout.setColorSchemeResources(
                R.color.apptheme_second,
                R.color.apptheme_main,
                R.color.apptheme_second,
                R.color.apptheme_main);
        mRefreshLayout.setOnRefreshListener(mWebViewController);

        mWebViewController.request(mUrl);

        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mCallback.isDrawerOpen())
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

