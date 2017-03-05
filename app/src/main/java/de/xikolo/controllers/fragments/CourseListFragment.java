package de.xikolo.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.controllers.CourseActivity;
import de.xikolo.controllers.CourseDetailsActivity;
import de.xikolo.controllers.adapters.CourseListAdapter;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.controllers.helper.LoadingStateController;
import de.xikolo.controllers.helper.RefeshLayoutHelper;
import de.xikolo.controllers.navigation.adapter.NavigationAdapter;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.managers.jobs.ListCoursesJob;
import de.xikolo.models.Course;
import de.xikolo.presenters.CourseListPresenter;
import de.xikolo.presenters.CourseListPresenterFactory;
import de.xikolo.presenters.CourseListView;
import de.xikolo.presenters.PresenterFactory;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;

import static de.xikolo.R.id.refreshLayout;

@FragmentWithArgs
public class CourseListFragment extends MainFragment<CourseListPresenter, CourseListView> implements CourseListView {

    public static final String TAG = CourseListFragment.class.getSimpleName();

    public static final String FILTER_ALL = "filter_all";
    public static final String FILTER_MY = "filter_my";

    @Arg String filter;

    @BindView(R.id.recyclerView) AutofitRecyclerView recyclerView;

    private CourseListAdapter courseListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);
        ButterKnife.bind(this, view);

        courseListAdapter = new CourseListAdapter(new CourseListAdapter.OnCourseButtonClickListener() {
            @Override
            public void onEnrollButtonClicked(String courseId) {
                presenter.onEnrollButtonClicked(courseId);
            }

            @Override
            public void onEnterButtonClicked(String courseId) {
                presenter.onCourseEnterButtonClicked(courseId);
            }

            @Override
            public void onDetailButtonClicked(String courseId) {
                presenter.onCourseDetailButtonClicked(courseId);
            }
        }, isAllCoursesFilter() ? CourseManager.CourseFilter.ALL : CourseManager.CourseFilter.MY);

        recyclerView.setHasFixedSize(true);
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (filter.equals(FILTER_ALL)) {
            activityCallback.onFragmentAttached(NavigationAdapter.NAV_ALL_COURSES.getPosition(), getString(R.string.title_section_all_courses));
        } else if (filter.equals(FILTER_MY)) {
            activityCallback.onFragmentAttached(NavigationAdapter.NAV_MY_COURSES.getPosition(), getString(R.string.title_section_my_courses));
        }

        presenter.onStart();
    }

    private void updateView(List<Course> courseList) {
        if (isAdded()) {
            if (isMyCoursesFilter() && !UserManager.isLoggedIn()) {
                courseList = null;

                notificationController.setTitle(R.string.notification_please_login);
                notificationController.setSummary(R.string.notification_please_login_summary);
                notificationController.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
                    }
                });
                notificationController.setNotificationVisible(true);
            } else if (isMyCoursesFilter() && (courseList == null || courseList.size() == 0)) {
                notificationController.setTitle(R.string.notification_no_enrollments);
                notificationController.setSummary(R.string.notification_no_enrollments_summary);
                notificationController.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activityCallback.selectDrawerSection(NavigationAdapter.NAV_ALL_COURSES.getPosition());
                    }
                });
                notificationController.setNotificationVisible(true);
            }

            activityCallback.updateDrawer();
        }
    }

    @Override
    public void onRefresh() {
        presenter.onRefresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (courseListAdapter != null) {
            courseListAdapter.destroy();
        }
    }

    @Override
    public void onEnrollButtonClicked(String courseId) {
        presenter.onEnrollButtonClicked(courseId);
    }

    private boolean isMyCoursesFilter() {
        return filter.equals(CourseListFragment.FILTER_MY);
    }

    private boolean isAllCoursesFilter() {
        return filter.equals(CourseListFragment.FILTER_ALL);
    }

    @Override
    public void onEnterButtonClicked(Course course) {
        if (UserManager.isLoggedIn()) {
            Intent intent = new Intent(getActivity(), CourseActivity.class);
            Bundle b = new Bundle();
//            b.putParcelable(CourseActivity.ARG_COURSE, course);
            intent.putExtras(b);
            startActivity(intent);
        } else {
            ToastUtil.show(R.string.toast_please_log_in);
            activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
        }
    }

    @Override
    public void onDetailButtonClicked(Course course) {
        Intent intent = new Intent(getActivity(), CourseDetailsActivity.class);
        Bundle b = new Bundle();
//        b.putParcelable(CourseDetailsActivity.ARG_COURSE, course);
        intent.putExtras(b);
        startActivity(intent);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCourseListJobEvent(ListCoursesJob.ListCoursesJobEvent event) {
        switch (event.getState()) {
            case SUCCESS:
                notificationController.setInvisible();
                refreshLayout.setRefreshing(false);
                break;
            case NO_NETWORK:
                NetworkUtil.showNoConnectionToast();
            case NO_AUTH:
            case CANCEL:
            case ERROR:
                refreshLayout.setRefreshing(false);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (activityCallback != null && !activityCallback.isDrawerOpen()) {
            inflater.inflate(R.menu.refresh, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseListPresenter> getPresenterFactory() {
        return new CourseListPresenterFactory();
    }

    @Override
    public void showLoginRequiredMessage() {
        super.showLoginRequiredMessage();
        loadingStateController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
            }
        });
    }

    @Override
    public void showNoEnrollmentsMessage() {
        loadingStateController.setTitle(R.string.notification_no_enrollments);
        loadingStateController.setSummary(R.string.notification_no_enrollments_summary);
        loadingStateController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCallback.selectDrawerSection(NavigationAdapter.NAV_ALL_COURSES.getPosition());
            }
        });
        loadingStateController.showMessage();
    }

}
