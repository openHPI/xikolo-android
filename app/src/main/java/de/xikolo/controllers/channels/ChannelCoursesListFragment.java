package de.xikolo.controllers.channels;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.yatatsu.autobundle.AutoBundleField;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.course.CourseActivityAutoBundle;
import de.xikolo.controllers.course.CourseDetailsActivityAutoBundle;
import de.xikolo.controllers.login.LoginActivityAutoBundle;
import de.xikolo.controllers.main.CourseListAdapter;
import de.xikolo.events.LoginEvent;
import de.xikolo.events.LogoutEvent;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.main.CourseListFilterChannelPresenterFactory;
import de.xikolo.presenters.main.CourseListPresenter;
import de.xikolo.presenters.main.CourseListView;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;

public class ChannelCoursesListFragment extends LoadingStatePresenterFragment<CourseListPresenter, CourseListView> implements CourseListView {

    public static final String TAG = ChannelCoursesListFragment.class.getSimpleName();

    @AutoBundleField
    String channelId;

    @BindView(R.id.content_view) AutofitRecyclerView recyclerView;

    private CourseListAdapter courseListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_course_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseListAdapter = new CourseListAdapter(this, new CourseListAdapter.OnCourseButtonClickListener() {
            @Override
            public void onEnrollButtonClicked(String courseId) {
                presenter.onEnrollButtonClicked(courseId);
            }

            @Override
            public void onContinueButtonClicked(String courseId) {
                presenter.onCourseEnterButtonClicked(courseId);
            }

            @Override
            public void onDetailButtonClicked(String courseId) {
                presenter.onCourseDetailButtonClicked(courseId);
            }
        }, Course.Filter.ALL);

        recyclerView.setAdapter(courseListAdapter);

        recyclerView.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return courseListAdapter.isHeader(position) ? recyclerView.getSpanCount() : 1;
            }
        });

        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getActivity().getResources().getDimensionPixelSize(R.dimen.card_horizontal_margin),
                getActivity().getResources().getDimensionPixelSize(R.dimen.card_vertical_margin),
                false,
                new SpaceItemDecoration.RecyclerViewInfo() {
                    @Override
                    public boolean isHeader(int position) {
                        return courseListAdapter.isHeader(position);
                    }

                    @Override
                    public int getSpanCount() {
                        return recyclerView.getSpanCount();
                    }

                    @Override
                    public int getItemCount() {
                        return courseListAdapter.getItemCount();
                    }
                }));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showCourseList(SectionList<String, List<Course>> courseList) {
        if (courseListAdapter != null) {
            courseListAdapter.update(courseList);
        }
    }

    @Override
    public void openLogin() {
        Intent intent = LoginActivityAutoBundle.builder().build(getActivity());
        startActivity(intent);
    }

    @Override
    public void enterCourse(String courseId) {
        Intent intent = CourseActivityAutoBundle.builder().courseId(courseId).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void enterCourseDetails(String courseId) {
        Intent intent = CourseDetailsActivityAutoBundle.builder(courseId).build(getActivity());
        startActivity(intent);
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseListPresenter> getPresenterFactory() {
          return new CourseListFilterChannelPresenterFactory(channelId);
    }

    @Override
    public void showNoEnrollmentsMessage() {
        loadingStateHelper.setMessageTitle(R.string.notification_no_enrollments);
        loadingStateHelper.setMessageSummary(R.string.notification_no_enrollments_summary);
        loadingStateHelper.showMessage();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        if (presenter != null) {
            presenter.onRefresh();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutEvent(LogoutEvent event) {
        if (presenter != null) {
            presenter.onRefresh();
        }
    }

}
