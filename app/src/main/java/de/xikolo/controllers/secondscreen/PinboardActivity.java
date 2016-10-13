package de.xikolo.controllers.secondscreen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import de.xikolo.R;
import de.xikolo.controllers.BaseActivity;
import de.xikolo.controllers.WebViewFragment;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Module;
import de.xikolo.utils.LanalyticsUtil;

public class PinboardActivity extends BaseActivity {

    public static final String TAG = PinboardActivity.class.getSimpleName();

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_URL = "arg_url";
    public static final String ARG_IN_APP_LINKS = "arg_in_app_links";
    public static final String ARG_EXTERNAL_LINKS = "arg_external_links";

    private String title;
    private String url;
    private boolean inAppLinksEnabled;
    private boolean externalLinksEnabled;

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    private Course course;
    private Module module;
    private Item item;

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
        this.course = b.getParcelable(ARG_COURSE);
        this.module = b.getParcelable(ARG_MODULE);
        this.item = b.getParcelable(ARG_ITEM);

        setTitle(title);

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, WebViewFragment.newInstance(url, inAppLinksEnabled, externalLinksEnabled), tag);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (course != null && module != null && item != null) {
            LanalyticsUtil.trackSecondScreenPinboardStart(item.id, course.id, module.id);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (course != null && module != null && item != null) {
            LanalyticsUtil.trackSecondScreenPinboardStop(item.id, course.id, module.id);
        }
    }

}
