package de.xikolo.controllers.course;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.helper.CollapsingToolbarHelper;
import de.xikolo.models.Course;
import de.xikolo.utils.ShareUtil;

public class CourseDetailsActivity extends BaseActivity {

    public static final String TAG = CourseDetailsActivity.class.getSimpleName();

    @AutoBundleField String courseId;

    @BindView(R.id.toolbar_image) ImageView imageView;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.stub_bottom) ViewStub stubBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_collapsing);
        setupActionBar(true);
        enableOfflineModeToolbar(false);

        Course course = Course.get(courseId);

        setTitle(course.title);

        String tag = "content";

        if (course.imageUrl != null) {
            GlideApp.with(this).load(course.imageUrl).into(imageView);
        } else {
            CollapsingToolbarHelper.lockCollapsingToolbar(course.title, appBarLayout, collapsingToolbar, toolbar, null, null);
        }

        final CourseDetailsFragment fragment = CourseDetailsFragmentAutoBundle.builder(courseId).build();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }

        if (course.enrollable && !course.isEnrolled()) {
            stubBottom.setLayoutResource(R.layout.content_enroll_button);
            Button enrollButton = (Button) stubBottom.inflate();
            enrollButton.setOnClickListener(view -> fragment.onEnrollButtonClicked());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_share:
                ShareUtil.shareCourseLink(this, courseId);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
