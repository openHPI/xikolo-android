package de.xikolo.controller.secondscreen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.BaseActivity;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.Subtitle;
import de.xikolo.util.LanalyticsUtil;

public class TranscriptViewerActivity extends BaseActivity {

    public static final String TAG = TranscriptViewerActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    public static final String ARG_SUBTITLES = "arg_subtitles";

    private Course course;
    private Module module;
    private Item item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript);
        setupActionBar();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle b = getIntent().getExtras();
        course = b.getParcelable(ARG_COURSE);
        module = b.getParcelable(ARG_MODULE);
        item = b.getParcelable(ARG_ITEM);
        List<Subtitle> subtitleList = b.getParcelableArrayList(ARG_SUBTITLES);

        setTitle(item.title + " - " + getString(R.string.second_screen_transcript));

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, TranscriptViewerFragment.newInstance(item, subtitleList), tag);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (course != null && module != null && item != null) {
            LanalyticsUtil.trackSecondScreenTranscriptStart(item.id, course.id, module.id);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (course != null && module != null && item != null) {
            LanalyticsUtil.trackSecondScreenTranscriptStop(item.id, course.id, module.id);
        }
    }

}
