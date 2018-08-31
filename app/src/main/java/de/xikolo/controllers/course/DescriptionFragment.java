package de.xikolo.controllers.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.DescriptionPresenter;
import de.xikolo.presenters.course.DescriptionPresenterFactory;
import de.xikolo.presenters.course.DescriptionView;
import de.xikolo.utils.MarkdownUtil;

public class DescriptionFragment extends LoadingStatePresenterFragment<DescriptionPresenter, DescriptionView> implements DescriptionView {

    public static final String TAG = DescriptionFragment.class.getSimpleName();

    @AutoBundleField String courseId;

    @BindView(R.id.layout_header) ImageView imageView;
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
        return R.layout.content_course_description;
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
        if (course.imageUrl != null) {
            GlideApp.with(this).load(course.imageUrl).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
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

    @NonNull
    @Override
    protected PresenterFactory<DescriptionPresenter> getPresenterFactory() {
        return new DescriptionPresenterFactory(courseId);
    }

}
