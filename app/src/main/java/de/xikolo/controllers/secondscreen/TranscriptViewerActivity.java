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
import de.xikolo.utils.LanalyticsUtil;

public class TranscriptViewerActivity extends BaseActivity {

    public static final String TAG = TranscriptViewerActivity.class.getSimpleName();

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript);
        setupActionBar();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Item item = Item.get(itemId);
        setTitle(item.title + " - " + getString(R.string.second_screen_transcript));

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = TranscriptViewerFragmentAutoBundle.builder(
                    courseId,
                    sectionId,
                    itemId
            ).build();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LanalyticsUtil.trackSecondScreenEvent(LanalyticsUtil.SecondScreenEvent.TRANSCRIPT_START, itemId, courseId, sectionId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LanalyticsUtil.trackSecondScreenEvent(LanalyticsUtil.SecondScreenEvent.TRANSCRIPT_STOP, itemId, courseId, sectionId);
    }

}
