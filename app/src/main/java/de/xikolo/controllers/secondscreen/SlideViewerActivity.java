package de.xikolo.controllers.secondscreen;

import android.os.Bundle;
import android.view.WindowManager;

import com.yatatsu.autobundle.AutoBundleField;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.models.Item;
import de.xikolo.models.dao.ItemDao;
import de.xikolo.utils.LanalyticsUtil;

public class SlideViewerActivity extends BaseActivity {

    public static final String TAG = SlideViewerActivity.class.getSimpleName();

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_no_scroll);
        setupActionBar();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Item item = ItemDao.Unmanaged.find(itemId);
        setTitle(item.title + " - " + getString(R.string.second_screen_slides));

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = SlideViewerFragmentAutoBundle.builder(itemId).build();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LanalyticsUtil.trackSecondScreenEvent(LanalyticsUtil.SecondScreenEvent.SLIDES_START, itemId, courseId, sectionId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LanalyticsUtil.trackSecondScreenEvent(LanalyticsUtil.SecondScreenEvent.SLIDES_STOP, itemId, courseId, sectionId);
    }

}
