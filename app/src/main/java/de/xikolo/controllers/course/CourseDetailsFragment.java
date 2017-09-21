package de.xikolo.controllers.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.controllers.login.LoginActivityAutoBundle;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.CourseDetailsPresenter;
import de.xikolo.presenters.course.CourseDetailsPresenterFactory;
import de.xikolo.presenters.course.CourseDetailsView;
import de.xikolo.utils.MarkdownUtil;
import de.xikolo.utils.ToastUtil;

public class CourseDetailsFragment extends LoadingStatePresenterFragment<CourseDetailsPresenter, CourseDetailsView> implements CourseDetailsView {

    public static final String TAG = CourseDetailsFragment.class.getSimpleName();

    @AutoBundleField String courseId;

    @BindView(R.id.layout_header) FrameLayout layoutHeader;
    @BindView(R.id.image_course) ImageView imageCourse;
    @BindView(R.id.text_title) TextView textTitle;
    @BindView(R.id.text_teacher) TextView textTeacher;
    @BindView(R.id.text_date) TextView textDate;
    @BindView(R.id.text_language) TextView textLanguage;
    @BindView(R.id.text_description) TextView textDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_course_details;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int videoId = item.getItemId();
        switch (videoId) {
            case R.id.action_refresh:
                presenter.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setupView(Course course) {
        if (getActivity() instanceof CourseDetailsActivity) {
            layoutHeader.setVisibility(View.GONE);
        } else {
            ImageHelper.load(course.imageUrl, imageCourse);
            textTitle.setText(course.title);
        }

        textDate.setText(course.getFormattedDate());
        textLanguage.setText(course.getFormattedLanguage());
        MarkdownUtil.formatAndSet(course.description, textDescription);

        if (course.teachers != null && !"".equals(course.teachers)) {
            textTeacher.setText(course.teachers);
        } else {
            textTeacher.setVisibility(View.GONE);
        }
    }

    @Override
    public void enterCourse(String courseId) {
        getActivity().finish();
        Intent intent = CourseActivityAutoBundle.builder().courseId(courseId).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void openLogin() {
        Intent intent = LoginActivityAutoBundle.builder().build(getActivity());
        startActivity(intent);
    }

    public void onEnrollButtonClicked() {
        presenter.enroll();
    }

    @Override
    public void showCourseNotAccessibleToast() {
        ToastUtil.show(R.string.btn_starts_soon);
    }

    @Override
    public void hideEnrollButton() {
        View button = getActivity().findViewById(R.id.button_enroll);
        if (button != null) {
            button.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseDetailsPresenter> getPresenterFactory() {
        return new CourseDetailsPresenterFactory(courseId);
    }

}
