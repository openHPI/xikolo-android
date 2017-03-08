package de.xikolo.controllers.module;

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
import de.xikolo.controllers.helper.WebViewHelper;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Module;
import de.xikolo.utils.Config;

public class ItemWebViewFragment extends PagerFragment {

    public static final String TAG = ItemWebViewFragment.class.getSimpleName();

    private View layout;

    private WebViewHelper webViewHelper;

    private MutableContextWrapper mutableContextWrapper;

    public ItemWebViewFragment() {
        // Required empty public constructor
    }

    public static PagerFragment newInstance(Course course, Module module, Item item) {
        return PagerFragment.newInstance(new ItemWebViewFragment(), course, module, item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                switch (item.type) {
                    case Item.TYPE_TEXT:
                    case Item.TYPE_VIDEO:
                    case Item.TYPE_LTI:
                        webViewHelper.setInAppLinksEnabled(false);
                        webViewHelper.setLoadExternalUrlEnabled(false);
                        break;
                    case Item.TYPE_SELFTEST:
                    case Item.TYPE_PEER:
                        webViewHelper.setInAppLinksEnabled(true);
                        webViewHelper.setLoadExternalUrlEnabled(false);
                        break;
                }

                webViewHelper.request(Config.URI + Config.COURSES + course.slug + "/" + Config.ITEMS + item.id, false);
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
