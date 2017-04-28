package de.xikolo.controllers.secondscreen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.utils.LanalyticsUtil;

public class SlideViewerActivity extends BaseActivity {

    public static final String TAG = SlideViewerActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    private Course course;
    private Section module;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_no_scroll);
        setupActionBar();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle b = getIntent().getExtras();
        course = b.getParcelable(ARG_COURSE);
        module = b.getParcelable(ARG_MODULE);
        item = b.getParcelable(ARG_ITEM);

        setTitle(item.title + " - " + getString(R.string.second_screen_slides));

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            transaction.replace(R.id.content, SlideViewerFragment.newInstance(course, module, item), tag);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (course != null && module != null && item != null) {
            LanalyticsUtil.trackSecondScreenSlidesStart(item.id, course.id, module.id);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (course != null && module != null && item != null) {
            LanalyticsUtil.trackSecondScreenSlidesStop(item.id, course.id, module.id);
        }
    }

}
