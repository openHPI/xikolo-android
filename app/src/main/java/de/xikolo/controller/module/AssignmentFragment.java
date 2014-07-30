package de.xikolo.controller.module;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import de.xikolo.R;
import de.xikolo.manager.ItemDetailManager;
import de.xikolo.manager.SessionManager;
import de.xikolo.model.ItemAssignment;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.Module;
import de.xikolo.util.Path;
import de.xikolo.util.Network;

public class AssignmentFragment extends PagerFragment<ItemAssignment> {

    public static final String TAG = AssignmentFragment.class.getSimpleName();

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private ItemDetailManager mItemManager;

    private SessionManager mSessionManager;

    public AssignmentFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new AssignmentFragment(), course, module, item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_assignment, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) layout.findViewById(R.id.progress);

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
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(Path.URI_HOST_HPI) || url.contains(Path.URI_HOST_SAP)) {
                    Map<String, String> header = new HashMap<String, String>();
                    header.put(Path.HEADER_USER_PLATFORM, Path.HEADER_VALUE_USER_PLATFORM_ANDROID);
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

        mItemManager = new ItemDetailManager(getActivity()) {
            @Override
            public void onItemDetailRequestReceived(Item item) {
                mItem = item;
                request();
            }

            @Override
            public void onItemDetailRequestCancelled() {
            }
        };

        mSessionManager = new SessionManager(getActivity()) {
            @Override
            public void onSessionRequestReceived() {
                request();
            }

            @Override
            public void onSessionRequestCancelled() {
            }
        };

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Network.isOnline(getActivity())) {
            Type type = new TypeToken<Item<ItemAssignment>>() {
            }.getType();
            mItemManager.requestItemDetail(mCourse, mModule, mItem, type, true);
        } else {
            Network.showNoConnectionToast(getActivity());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
//            case R.id.action_refresh:
//                onRefresh();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void request() {
        Log.d(TAG, "request");

        if (Network.isOnline(getActivity())) {
            if (SessionManager.hasSession(getActivity())) {
                Map<String, String> header = new HashMap<String, String>();
                header.put(Path.HEADER_USER_PLATFORM, Path.HEADER_VALUE_USER_PLATFORM_ANDROID);
                mWebView.loadUrl(mItem.object.url, header);
            } else {
                mSessionManager.createSession();
            }
        } else {
            Network.showNoConnectionToast(getActivity());
        }
    }

    @Override
    public void pageChanged() {

    }

    @Override
    public void pageScrolling(int state) {

    }

}
