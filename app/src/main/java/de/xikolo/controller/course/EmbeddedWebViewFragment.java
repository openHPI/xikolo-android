package de.xikolo.controller.course;

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
import de.xikolo.controller.BaseFragment;
import de.xikolo.controller.helper.WebViewController;

public class EmbeddedWebViewFragment extends BaseFragment {

    public static final String TAG = EmbeddedWebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "arg_url";
    private static final String ARG_INAPPLINKS = "arg_inapplinks";

    private String mUrl;
    private boolean mInAppLinksEnabled;

    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mProgress;

    private WebViewController mWebViewController;

    public EmbeddedWebViewFragment() {
        // Required empty public constructor
    }

    public static EmbeddedWebViewFragment newInstance(String url, boolean inAppLinksEnabled) {
        EmbeddedWebViewFragment fragment = new EmbeddedWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putBoolean(ARG_INAPPLINKS, inAppLinksEnabled);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_URL);
            mInAppLinksEnabled = getArguments().getBoolean(ARG_INAPPLINKS);
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

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mWebViewController.request(mUrl);
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

}

