package de.xikolo.controllers.announcement;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.course.CourseActivityAutoBundle;
import de.xikolo.models.Announcement;
import de.xikolo.presenters.announcement.AnnouncementPresenter;
import de.xikolo.presenters.announcement.AnnouncementPresenterFactory;
import de.xikolo.presenters.announcement.AnnouncementView;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.utils.MarkdownUtil;

public class AnnouncementFragment extends LoadingStatePresenterFragment<AnnouncementPresenter, AnnouncementView> implements AnnouncementView {

    public static final String TAG = AnnouncementFragment.class.getSimpleName();

    @AutoBundleField String announcementId;
    @AutoBundleField boolean global;

    @BindView(R.id.text) TextView text;
    @BindView(R.id.date) TextView date;
    @BindView(R.id.course_button) Button courseButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_announcement;
    }

    @Override
    public void showAnnouncement(Announcement announcement) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        date.setText(dateFormat.format(announcement.publishedAt));

        MarkdownUtil.formatAndSet(announcement.text, text);
    }

    @Override
    public void enableCourseButton() {
        courseButton.setVisibility(View.VISIBLE);
        courseButton.setOnClickListener((v) -> presenter.onCourseButtonClicked());
    }

    @NonNull
    @Override
    protected PresenterFactory<AnnouncementPresenter> getPresenterFactory() {
        return new AnnouncementPresenterFactory(announcementId, global);
    }

    @Override
    public void enterCourse(String courseId) {
        Intent intent = CourseActivityAutoBundle.builder(courseId).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
