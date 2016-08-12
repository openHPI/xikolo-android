package de.xikolo.controller.secondscreen;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.BaseActivity;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Subtitle;

public class TranscriptViewerActivity extends BaseActivity {

    public static final String TAG = TranscriptViewerActivity.class.getSimpleName();

    public static final String ARG_ITEM = "arg_item";
    public static final String ARG_SUBTITLES = "arg_subtitles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript);
        setupActionBar();

        Bundle b = getIntent().getExtras();
        Item item = b.getParcelable(ARG_ITEM);
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

}
