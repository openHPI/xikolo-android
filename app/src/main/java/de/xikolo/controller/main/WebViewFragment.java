package de.xikolo.controller.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.model.OnModelResponseListener;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class WebViewFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = WebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "arg_url";
    private static final String ARG_TOP_LEVEL_CONTENT = "arg_top_level_content";
    private static final String ARG_TITLE = "arg_title";

    private String mUrl;
    private String mTitle;
    private boolean isTopLevelContent;

    private SwipeRefreshLayout mRefreshLayout;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private UserModel mUserModel;

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

        mUserModel = new UserModel(getActivity(), jobManager);
        mUserModel.setCreateSessionListener(new OnModelResponseListener<Void>() {
            @Override
            public void onResponse(Void response) {
                if (UserModel.hasSession()) {
                    request();
                } else {
                    mUserModel.createSession();
                }
            }
        });
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
                R.color.apptheme_second,
                R.color.apptheme_main,
                R.color.apptheme_second,
                R.color.apptheme_main);
        mRefreshLayout.setOnRefreshListener(this);

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
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);

        if (NetworkUtil.isOnline(getActivity())) {
            if (UserModel.hasSession()) {
                request();
            } else {
                mUserModel.createSession();
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

