package de.xikolo.controller.secondscreen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import de.xikolo.R;
import de.xikolo.controller.BaseActivity;
import de.xikolo.controller.WebViewFragment;

public class WebViewActivity extends BaseActivity {

    public static final String TAG = WebViewActivity.class.getSimpleName();

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_URL = "arg_url";
    public static final String ARG_IN_APP_LINKS = "arg_in_app_links";
    public static final String ARG_EXTERNAL_LINKS = "arg_external_links";

    private String title;
    private String url;
    private boolean inAppLinksEnabled;
    private boolean externalLinksEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        Bundle b = getIntent().getExtras();
        this.title = b.getString(ARG_TITLE);
        this.url = b.getString(ARG_URL);
        this.inAppLinksEnabled = b.getBoolean(ARG_IN_APP_LINKS);
        this.externalLinksEnabled = b.getBoolean(ARG_EXTERNAL_LINKS);

        setTitle(title);

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, WebViewFragment.newInstance(url, inAppLinksEnabled, externalLinksEnabled), tag);
            transaction.commit();
        }
    }

}
