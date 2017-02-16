package de.xikolo.controllers.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.controllers.CourseActivity;
import de.xikolo.controllers.CourseDetailsActivity;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.controllers.helper.NotificationController;
import de.xikolo.controllers.helper.RefeshLayoutController;
import de.xikolo.controllers.main.adapter.CourseListAdapter;
import de.xikolo.controllers.navigation.adapter.NavigationAdapter;
import de.xikolo.events.EnrollEvent;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.managers.jobs.ListCoursesJob;
import de.xikolo.managers.jobs.NetworkJobEvent;
import de.xikolo.models.Course;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CourseListFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener,
        CourseListAdapter.OnCourseButtonClickListener {

    public static final String TAG = CourseListFragment.class.getSimpleName();

    public static final String ARG_FILTER = "arg_filter";

    public static final String FILTER_ALL = "filter_all";
    public static final String FILTER_MY = "filter_my";

    private String filter;

    @BindView(R.id.refreshLayout) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView) AutofitRecyclerView recyclerView;

    private CourseListAdapter courseListAdapter;

    private NotificationController notificationController;

    private RealmResults courseListPromise;

    private CourseManager courseManager;

    public CourseListFragment() {
        // Required empty public constructor
    }

    public static CourseListFragment newInstance(String filter) {
        CourseListFragment fragment = new CourseListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            filter = getArguments().getString(ARG_FILTER);
        }
        setHasOptionsMenu(true);

        courseManager = new CourseManager(jobManager);

        EventBus.getDefault().register(this);
    }

    private void requestCourses() {
        if (isMyCoursesFilter() && !UserManager.isLoggedIn()) {
            notificationController.setTitle(R.string.notification_please_login);
            notificationController.setSummary(R.string.notification_please_login_summary);
            notificationController.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
                }
            });
            notificationController.setNotificationVisible(true);
            refreshLayout.setRefreshing(false);
        } else {
            refreshLayout.setRefreshing(true);
            if (isMyCoursesFilter()) {
                courseManager.requestCourses();
            } else {
                courseManager.requestCourses();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_course_list, container, false);
        ButterKnife.bind(this, layout);

        RefeshLayoutController.setup(refreshLayout, this);

        if (isAllCoursesFilter()) {
            courseListAdapter = new CourseListAdapter(this, CourseManager.CourseFilter.ALL);
        } else if (isMyCoursesFilter()) {
            courseListAdapter = new CourseListAdapter(this, CourseManager.CourseFilter.MY);
        }

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

        notificationController = new NotificationController(layout);
        notificationController.setInvisible();

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (filter.equals(FILTER_ALL)) {
            activityCallback.onFragmentAttached(NavigationAdapter.NAV_ALL_COURSES.getPosition(), getString(R.string.title_section_all_courses));
        } else if (filter.equals(FILTER_MY)) {
            activityCallback.onFragmentAttached(NavigationAdapter.NAV_MY_COURSES.getPosition(), getString(R.string.title_section_my_courses));
        }

        courseListPromise = courseManager.listCourses(realm, new RealmChangeListener<RealmResults<Course>>() {
            @Override
            public void onChange(RealmResults<Course> result) {
                notificationController.setInvisible();
                updateView(result);
            }
        });

        requestCourses();
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

            if (courseList != null) {
                courseListAdapter.updateCourses(courseList);
            } else {
                courseListAdapter.clear();
            }
            activityCallback.updateDrawer();
        }
    }

    @Override
    public void onRefresh() {
        requestCourses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        if (courseListPromise != null) {
            courseListPromise.removeChangeListeners();
        }
    }

    @Override
    public void onEnrollButtonClicked(Course course) {
        final ProgressDialog dialog = ProgressDialog.getInstance();
        Result<Course> result = new Result<Course>() {
            @Override
            protected void onSuccess(Course result, DataSource dataSource) {
                dialog.dismiss();
                EventBus.getDefault().post(new EnrollEvent(result));
                if (DateUtil.nowIsAfter(result.startDate)) {
                    onEnterButtonClicked(result);
                }
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                dialog.dismiss();
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                } else if (errorCode == ErrorCode.NO_AUTH) {
                    ToastUtil.show(R.string.toast_please_log_in);
                    activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
                }
            }
        };
        dialog.show(getChildFragmentManager(), ProgressDialog.TAG);
        courseManager.addEnrollment(result, course);
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
            case WARNING:
                break;
            case ERROR:
                refreshLayout.setRefreshing(false);
                if (event.getCode() == NetworkJobEvent.Code.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                }
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

}
