package de.xikolo.controller.course;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
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

import java.util.HashMap;
import java.util.Map;

import de.xikolo.R;
import de.xikolo.manager.SessionManager;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class EmbeddedWebViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = EmbeddedWebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "arg_url";

    private String mUrl;

    private SwipeRefreshLayout mRefreshLayout;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private SessionManager mSessionManager;

    public EmbeddedWebViewFragment() {
        // Required empty public constructor
    }

    public static EmbeddedWebViewFragment newInstance(String url) {
        EmbeddedWebViewFragment fragment = new EmbeddedWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_URL);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) layout.findViewById(R.id.progress);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);

        final Activity activity = getActivity();

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "An error occurred" + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mRefreshLayout.setRefreshing(true);
                mWebView.setVisibility(View.GONE);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mRefreshLayout.setRefreshing(false);
                mProgressBar.setVisibility(ProgressBar.GONE);
                mWebView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Config.URI_HOST_HPI) || url.contains(Config.URI_HOST_SAP)) {
                    Map<String, String> header = new HashMap<String, String>();
                    header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_VALUE_USER_PLATFORM_ANDROID);
                    view.loadUrl(url, header);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
                return true;
            }

        });

        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((i == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
                return false;
            }
        });

        mRefreshLayout.setColorSchemeResources(
                R.color.red,
                R.color.orange,
                R.color.red,
                R.color.orange);
        mRefreshLayout.setOnRefreshListener(this);

        mSessionManager = new SessionManager(getActivity()) {
            @Override
            public void onSessionRequestReceived() {
                request();
            }

            @Override
            public void onSessionRequestCancelled() {
            }
        };

        onRefresh();

        return layout;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        if (savedInstanceState != null && mWebView != null) {
//            mWebView.restoreState(savedInstanceState);
//        } else {
//            onRefresh();
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (mWebView != null)
//            mWebView.saveState(outState);
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);

        if (NetworkUtil.isOnline(getActivity())) {
            if (SessionManager.hasSession(getActivity())) {
                request();
            } else {
                mSessionManager.createSession();
            }
        } else {
            mRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(ProgressBar.GONE);
            NetworkUtil.showNoConnectionToast(getActivity());
        }
    }

    private void request() {
        if (NetworkUtil.isOnline(getActivity())) {
            Map<String, String> header = new HashMap<String, String>();
            header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_VALUE_USER_PLATFORM_ANDROID);
            mWebView.loadUrl(mUrl, header);
        } else {
            mRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(ProgressBar.GONE);
            NetworkUtil.showNoConnectionToast(getActivity());
        }
    }

}

