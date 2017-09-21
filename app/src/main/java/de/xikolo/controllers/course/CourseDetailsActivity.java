package de.xikolo.controllers.course;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.models.Course;
import de.xikolo.utils.AndroidDimenUtil;

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
            ImageHelper.load(course.imageUrl, imageView);
        } else {
            lockCollapsingToolbar(course.title);
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
            enrollButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.onEnrollButtonClicked();
                }
            });
        }
    }

    private void lockCollapsingToolbar(String title) {
        appBarLayout.setExpanded(false, false);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        lp.height = AndroidDimenUtil.getActionBarHeight() + AndroidDimenUtil.getStatusBarHeight();
        collapsingToolbar.setTitleEnabled(false);
        toolbar.setTitle(title);
    }

}
