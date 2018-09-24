package de.xikolo.controllers.secondscreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle;
import de.xikolo.utils.LanalyticsUtil;

public class QuizActivity extends BaseActivity {

    public static final String TAG = QuizActivity.class.getSimpleName();

    @AutoBundleField String title;
    @AutoBundleField String url;
    @AutoBundleField boolean inAppLinksEnabled;
    @AutoBundleField boolean externalLinksEnabled;

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();

        setTitle(title);

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = WebViewFragmentAutoBundle.builder(url)
                    .inAppLinksEnabled(inAppLinksEnabled)
                    .externalLinksEnabled(externalLinksEnabled)
                    .build();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LanalyticsUtil.trackSecondScreenEvent(LanalyticsUtil.SecondScreenEvent.QUIZ_START, itemId, courseId, sectionId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LanalyticsUtil.trackSecondScreenEvent(LanalyticsUtil.SecondScreenEvent.QUIZ_STOP, itemId, courseId, sectionId);
    }

}
