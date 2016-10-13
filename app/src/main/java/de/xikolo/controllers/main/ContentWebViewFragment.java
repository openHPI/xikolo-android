package de.xikolo.controllers.main;

import android.content.MutableContextWrapper;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.helper.WebViewController;

public class ContentWebViewFragment extends ContentFragment {

    public static final String TAG = ContentWebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "arg_url";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_IN_APP_LINKS = "arg_in_app_links";
    private static final String ARG_EXTERNAL_LINKS = "arg_external_links";
    private static final String ARG_ID = "arg_id";

    private String url;
    private String title;
    private boolean inAppLinksEnabled;
    private boolean externalLinksEnabled;
    private int id;

    private View layout;

    private WebViewController webViewController;

    private MutableContextWrapper mutableContextWrapper;

    public ContentWebViewFragment() {
        // Required empty public constructor
    }

    public static ContentWebViewFragment newInstance(int id, String url, String title, boolean inAppLinksEnabled, boolean externalLinksEnabled) {
        ContentWebViewFragment fragment = new ContentWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_IN_APP_LINKS, inAppLinksEnabled);
        args.putBoolean(ARG_EXTERNAL_LINKS, externalLinksEnabled);
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(ARG_URL);
            title = getArguments().getString(ARG_TITLE);
            inAppLinksEnabled = getArguments().getBoolean(ARG_IN_APP_LINKS);
            externalLinksEnabled = getArguments().getBoolean(ARG_EXTERNAL_LINKS);
            id = getArguments().getInt(ARG_ID);
        }
        setHasOptionsMenu(true);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (layout == null) {
            mutableContextWrapper = new MutableContextWrapper(getActivity());

            layout = LayoutInflater.from(mutableContextWrapper)
                    .inflate(R.layout.fragment_webview, container, false);

            webViewController = new WebViewController(mutableContextWrapper, layout);
            webViewController.setInAppLinksEnabled(inAppLinksEnabled);
            webViewController.setLoadExternalUrlEnabled(externalLinksEnabled);

            webViewController.request(url, false);
        } else {
            mutableContextWrapper.setBaseContext(getActivity());
        }

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        activityCallback.onFragmentAttached(id, title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getRetainInstance() && layout.getParent() instanceof ViewGroup) {
            ((ViewGroup) layout.getParent()).removeView(layout);
            mutableContextWrapper.setBaseContext(GlobalApplication.getInstance());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (activityCallback != null && !activityCallback.isDrawerOpen()) {
            inflater.inflate(R.menu.refresh, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.action_refresh:
                webViewController.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
