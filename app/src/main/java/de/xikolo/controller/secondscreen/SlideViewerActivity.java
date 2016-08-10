package de.xikolo.controller.secondscreen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import de.xikolo.R;
import de.xikolo.controller.BaseActivity;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;

public class SlideViewerActivity extends BaseActivity {

    public static final String TAG = SlideViewerActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        Bundle b = getIntent().getExtras();
        Course course = b.getParcelable(ARG_COURSE);
        Module module = b.getParcelable(ARG_MODULE);
        Item item = b.getParcelable(ARG_ITEM);

        setTitle(item.title + " " + getString(R.string.second_screen_slides));

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, SlideViewerFragment.newInstance(course, module, item), tag);
            transaction.commit();
        }
    }

}
