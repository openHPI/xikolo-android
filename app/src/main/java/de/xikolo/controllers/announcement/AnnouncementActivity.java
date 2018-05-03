package de.xikolo.controllers.announcement;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.helper.CollapsingToolbarHelper;
import de.xikolo.models.Announcement;
import de.xikolo.models.Course;

import static de.xikolo.R.id.appbar;
import static de.xikolo.R.id.collapsing_toolbar;

public class AnnouncementActivity extends BaseActivity {

    public static final String TAG = AnnouncementActivity.class.getSimpleName();

    @AutoBundleField String announcementId;
    @AutoBundleField boolean global;

    @BindView(R.id.toolbar_image) ImageView imageView;
    @BindView(appbar) AppBarLayout appBarLayout;
    @BindView(collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.scrim_top) View scrimTop;
    @BindView(R.id.scrim_bottom) View scrimBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_collapsing);
        setupActionBar(true);
        enableOfflineModeToolbar(false);

        Announcement announcement = Announcement.get(announcementId);
        setTitle(announcement.title);

        if (announcement.imageUrl != null) {
            GlideApp.with(this).load(announcement.imageUrl).into(imageView);
        } else if (announcement.courseId != null) {
            Course course = Course.get(announcement.courseId);
            if (course.imageUrl != null) {
                GlideApp.with(this).load(course.imageUrl).into(imageView);
            } else {
                CollapsingToolbarHelper.lockCollapsingToolbar(announcement.title, appBarLayout, collapsingToolbar, toolbar, scrimTop, scrimBottom);
            }
        } else {
            CollapsingToolbarHelper.lockCollapsingToolbar(announcement.title, appBarLayout, collapsingToolbar, toolbar, scrimTop, scrimBottom);
        }

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, AnnouncementFragmentAutoBundle.builder(announcementId, global).build(), tag);
            transaction.commit();
        }
    }

}
