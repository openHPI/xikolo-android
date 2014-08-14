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

import java.util.HashMap;
import java.util.Map;

import de.xikolo.R;
import de.xikolo.entities.Course;
import de.xikolo.entities.Item;
import de.xikolo.entities.ItemAssignment;
import de.xikolo.entities.Module;
import de.xikolo.model.ItemModel;
import de.xikolo.model.OnModelResponseListener;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class AssignmentFragment extends PagerFragment<ItemAssignment> {

    public static final String TAG = AssignmentFragment.class.getSimpleName();

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private ItemModel mItemModel;

    private UserModel mUserModel;

    public AssignmentFragment() {

    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new AssignmentFragment(), course, module, item);
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
                    request();
                }
            }
        });

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

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (NetworkUtil.isOnline(getActivity())) {
            mItemModel.retrieveItemDetail(mCourse.id, mModule.id, mItem.id, Item.TYPE_ASSIGNMENT, true);
        } else {
            NetworkUtil.showNoConnectionToast(getActivity());
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

        if (NetworkUtil.isOnline(getActivity())) {
            if (UserModel.hasSession()) {
                Map<String, String> header = new HashMap<String, String>();
                header.put(Config.HEADER_USER_PLATFORM, Config.HEADER_VALUE_USER_PLATFORM_ANDROID);
                mWebView.loadUrl(mItem.object.url, header);
            } else {
                mUserModel.createSession();
            }
        } else {
            NetworkUtil.showNoConnectionToast(getActivity());
        }
    }

    @Override
    public void pageChanged() {

    }

    @Override
    public void pageScrolling(int state) {

    }

}
