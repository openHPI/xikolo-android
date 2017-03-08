package de.xikolo.controllers.fragments;

import android.content.Intent;
import android.content.MutableContextWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import java.util.Map;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.helper.WebViewHelper;
import de.xikolo.presenters.MainWebView;
import de.xikolo.presenters.PresenterFactory;
import de.xikolo.presenters.WebViewPresenter;
import de.xikolo.presenters.WebViewPresenterFactory;
import de.xikolo.utils.Config;
import de.xikolo.utils.ToastUtil;

@FragmentWithArgs
public class MainWebViewFragment extends MainFragment<WebViewPresenter, MainWebView> implements MainWebView {

    public static final String TAG = MainWebViewFragment.class.getSimpleName();

    @Arg int id;
    @Arg String url;
    @Arg String title;
    @Arg(required = false) boolean inAppLinksEnabled;
    @Arg(required = false) boolean externalLinksEnabled;

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

            webViewHelper = new WebViewHelper(view);
        } else {
            mutableContextWrapper.setBaseContext(getActivity());
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        activityCallback.onFragmentAttached(id, title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getRetainInstance() && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
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
                presenter.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    protected PresenterFactory<WebViewPresenter> getPresenterFactory() {
        return new WebViewPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull WebViewPresenter presenter) {
        webViewHelper.setup(presenter, url);
    }

    @Override
    public void showInvalidUrlToast() {
        ToastUtil.show(R.string.notification_url_invalid);
    }

    @Override
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
    public void loadUrl(String url, Map<String, String> header) {
        webViewHelper.loadUrl(url, header);
    }

    @Override
    public boolean webViewIsShown() {
        return webViewHelper.webViewIsShown();
    }

    @Override
    public void showWebView() {
        webViewHelper.showWebView();
    }

    @Override
    public void hideWebView() {
        webViewHelper.hideWebView();
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

}
