package de.xikolo.openhpi.controller.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.util.Network;

public class WebViewFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = WebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "url";

    private String mUrl;

    private SwipeRefreshLayout mRefreshLayout;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private boolean mFirstLoad;

    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment newInstance(String url) {
        WebViewFragment fragment = new WebViewFragment();
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
    public void onStart() {
        super.onStart();
        if (mUrl.equals(getString(R.string.url_news))) {
            mCallback.onFragmentAttached(1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) layout.findViewById(R.id.progress);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);

        mFirstLoad = true;

        final Activity activity = getActivity();

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && mFirstLoad) {
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
                if (progress == 100) {
                    mProgressBar.setVisibility(ProgressBar.GONE);
                    mFirstLoad = false;
                }
            }

        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "An error occurred" + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mRefreshLayout.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(getString(R.string.url_openhpi_base))) {
                    view.loadUrl(url);
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
                return true;
            }
        });

        mRefreshLayout.setColorScheme(R.drawable.orange,
                R.drawable.red,
                R.drawable.yellow,
                R.drawable.red);
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
        if (!mDrawerOpen)
            inflater.inflate(R.menu.webview, menu);
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
        Log.d(TAG, "onRefresh");

        mWebView.loadUrl("about:blank");
        mRefreshLayout.setRefreshing(true);

        if (Network.isOnline(getActivity())) {
            Map<String, String> header = new HashMap<String, String>();
            header.put("User-Platform", "Android");
            mWebView.loadUrl(mUrl, header);
        } else {
            // TODO better no network handling
            String html = "" +
                    "<html>" +
                    "<body>" +
                    "<div align=\"center\"><br>" +
                    getString(R.string.no_network) +
                    "<div>" +
                    "</body>" +
                    "</html>";
            mWebView.loadData(html, "text/html", "utf-8");
        }
    }

}
