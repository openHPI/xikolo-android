package de.xikolo.controllers.announcement;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.models.Announcement;
import de.xikolo.models.Course;

public class AnnouncementActivity extends BaseActivity {

    public static final String TAG = AnnouncementActivity.class.getSimpleName();

    @AutoBundleField String announcementId;

    @BindView(R.id.toolbar_image) ImageView imageView;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_collapsing);
        setupActionBar();

        Announcement announcement = Announcement.get(announcementId);
        setTitle(announcement.title);

        if (announcement.imageUrl != null) {
            ImageHelper.load(announcement.imageUrl, imageView);
        } else if (announcement.courseId != null) {
            Course course = Course.get(announcement.courseId);
            if (course.imageUrl != null) {
                ImageHelper.load(course.imageUrl, imageView);
            } else {
                appBarLayout.setExpanded(false, false);
            }
        } else {
            appBarLayout.setExpanded(false, false);
        }

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, AnnouncementFragmentAutoBundle.builder(announcementId).build(), tag);
            transaction.commit();
        }
    }

}
