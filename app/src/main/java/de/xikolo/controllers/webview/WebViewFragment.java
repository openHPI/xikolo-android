package de.xikolo.controllers.webview;

import android.content.Intent;
import android.content.MutableContextWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.controllers.base.LoadingStateFragment;
import de.xikolo.controllers.helper.WebViewHelper;
import de.xikolo.utils.ToastUtil;

public class WebViewFragment extends LoadingStateFragment implements WebViewInterface {

    public static final String TAG = WebViewFragment.class.getSimpleName();

    @AutoBundleField String url;
    @AutoBundleField(required = false) boolean inAppLinksEnabled;
    @AutoBundleField(required = false) boolean externalLinksEnabled;

    private View view;

    private WebViewHelper webViewHelper;

    private MutableContextWrapper mutableContextWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            mutableContextWrapper = new MutableContextWrapper(getActivity());

            view = LayoutInflater.from(mutableContextWrapper).inflate(R.layout.fragment_webview, container, false);

            webViewHelper = new WebViewHelper(view, this);
        } else {
            mutableContextWrapper.setBaseContext(getActivity());
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webViewHelper.request(url, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getRetainInstance() && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
            mutableContextWrapper.setBaseContext(App.getInstance());
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
                webViewHelper.refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showInvalidUrlToast() {
        ToastUtil.show(R.string.notification_url_invalid);
    }

    public void showErrorToast(String message) {
        ToastUtil.show("An error occurred: " + message);
    }

    @Override
    public boolean inAppLinksEnabled() {
        return inAppLinksEnabled;
    }

    @Override
    public boolean externalLinksEnabled() {
        return externalLinksEnabled;
    }

    @Override
    public void openUrlInBrowser(Uri uri, String token) {
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        if (token != null) {
            Bundle headers = new Bundle();
            headers.putString(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_PREFIX + token);
            i.putExtra(Browser.EXTRA_HEADERS, headers);
        }
        getActivity().startActivity(i);
    }

    @Override
    public void onRefresh() {
        webViewHelper.refresh();
    }

}
