package de.xikolo.controllers;

import android.content.MutableContextWrapper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.fragments.BaseFragment;
import de.xikolo.controllers.helper.WebViewHelper;

public class WebViewFragment extends BaseFragment {

    public static final String TAG = WebViewFragment.class.getSimpleName();

    // the fragment initialization parameters
    private static final String ARG_URL = "arg_url";
    private static final String ARG_IN_APP_LINKS = "arg_in_app_links";
    private static final String ARG_EXTERNAL_LINKS = "arg_external_links";

    private String url;
    private boolean inAppLinksEnabled;
    private boolean externalLinksEnabled;

    private View layout;

    private WebViewHelper webViewHelper;

    private MutableContextWrapper mutableContextWrapper;

    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment newInstance(String url, boolean inAppLinksEnabled, boolean externalLinksEnabled) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putBoolean(ARG_IN_APP_LINKS, inAppLinksEnabled);
        args.putBoolean(ARG_EXTERNAL_LINKS, externalLinksEnabled);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(ARG_URL);
            inAppLinksEnabled = getArguments().getBoolean(ARG_IN_APP_LINKS);
            externalLinksEnabled = getArguments().getBoolean(ARG_EXTERNAL_LINKS);
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

            webViewHelper = new WebViewHelper(mutableContextWrapper, layout);
            webViewHelper.setInAppLinksEnabled(inAppLinksEnabled);
            webViewHelper.setLoadExternalUrlEnabled(externalLinksEnabled);

            webViewHelper.request(url, false);
        } else {
            mutableContextWrapper.setBaseContext(getActivity());
        }

        return layout;
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
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                webViewHelper.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

