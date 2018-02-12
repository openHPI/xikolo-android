package de.xikolo.controllers.webview;

import android.content.Intent;
import android.content.MutableContextWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.crashlytics.android.Crashlytics;
import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.helper.WebViewHelper;
import de.xikolo.controllers.login.LoginActivityAutoBundle;
import de.xikolo.presenters.base.LoadingStatePresenter;
import de.xikolo.presenters.base.Presenter;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;

public class WebViewFragment extends LoadingStatePresenterFragment implements WebViewInterface {

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
    public int getLayoutResource() {
        return R.layout.content_webview;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            mutableContextWrapper = new MutableContextWrapper(getActivity());

            view = LayoutInflater.from(mutableContextWrapper).inflate(R.layout.fragment_loading_state, container, false);
            // inflate content view inside
            ViewStub contentView = view.findViewById(R.id.content_view);
            contentView.setLayoutResource(getLayoutResource());
            contentView.inflate();

            webViewHelper = new WebViewHelper(view, this);
        } else {
            mutableContextWrapper.setBaseContext(getActivity());
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!NetworkUtil.isOnline()) {
            showNetworkRequiredMessage();
        } else if (webViewHelper.requestedUrl() == null) {
            webViewHelper.request(url);
        } else {
            webViewHelper.showWebView();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getRetainInstance() && view.getParent() instanceof ViewGroup) {
            try {
                ((ViewGroup) view.getParent()).removeView(view);
                mutableContextWrapper.setBaseContext(App.getInstance());
            } catch (Exception e) {
                Crashlytics.logException(e);
                view = null;
                mutableContextWrapper = null;
            }
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
            headers.putString(Config.HEADER_AUTH, Config.HEADER_AUTH_VALUE_PREFIX + token);
            i.putExtra(Browser.EXTRA_HEADERS, headers);
        }
        getActivity().startActivity(i);
    }

    @Override
    public void interceptSSOLogin(String token) {
        Intent intent = LoginActivityAutoBundle.builder().token(token).build(getActivity());
        getActivity().startActivity(intent);
    }

    @Override
    public void onRefresh() {
        if (webViewHelper.requestedUrl() != null) {
            webViewHelper.refresh();
        } else {
            webViewHelper.request(url);
        }
    }

    // quite hacky, we want the loading state capabilities but don't need a presenter
    @NonNull
    @Override
    protected PresenterFactory getPresenterFactory() {
        return new PresenterFactory() {
            @Override
            public Presenter create() {
                return new LoadingStatePresenter() {
                    @Override
                    public void onRefresh() {}
                };
            }
        };
    }

}
