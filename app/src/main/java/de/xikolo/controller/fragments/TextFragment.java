package de.xikolo.controller.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import de.xikolo.R;
import de.xikolo.manager.ItemObjectManager;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.Module;
import de.xikolo.model.Text;
import de.xikolo.util.Network;

public class TextFragment extends Fragment {

    public static final String TAG = TextFragment.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    private Course mCourse;
    private Module mModule;
    private Item<Text> mItem;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    private ItemObjectManager mItemManager;

    public TextFragment() {
        // Required empty public constructor
    }

    public static TextFragment newInstance(Course course, Module module, Item item) {
        TextFragment fragment = new TextFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COURSE, course);
        args.putParcelable(ARG_MODULE, module);
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = getArguments().getParcelable(ARG_COURSE);
            mModule = getArguments().getParcelable(ARG_MODULE);
            mItem = getArguments().getParcelable(ARG_ITEM);
        }
//        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_text, container, false);
        mWebView = (WebView) layout.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) layout.findViewById(R.id.progress);

        final Activity activity = getActivity();

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100) {
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
                if (progress == 100) {
                    mProgressBar.setVisibility(ProgressBar.GONE);
                }
            }

        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "An error occurred" + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (url.contains(Config.URI_HOST_HPI)) {
//                    view.loadUrl(url);
//                } else {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
//                }
                return true;
            }
        });

        mItemManager = new ItemObjectManager(getActivity()) {
            @Override
            public void onItemRequestReceived(Item item) {
                mItem = item;
                displayBody();
            }

            @Override
            public void onItemRequestCancelled() {
            }
        };

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Network.isOnline(getActivity())) {
            Type type = new TypeToken<Item<Text>>() {
            }.getType();
            mItemManager.requestItemObject(mCourse, mModule, mItem, type, true);
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

    public void displayBody() {
        Log.d(TAG, "displayBody");

        mWebView.loadData(mItem.object.body, "text/html; charset=UTF-8", null);
    }

}
