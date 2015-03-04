package de.xikolo.controller.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.CourseActivity;
import de.xikolo.controller.CourseDetailsActivity;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.controller.helper.RefeshLayoutController;
import de.xikolo.controller.main.adapter.CourseListAdapter;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class CourseListFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener,
        CourseListAdapter.OnCourseButtonClickListener {

    public static final String TAG = CourseListFragment.class.getSimpleName();

    public static final String ARG_FILTER = "arg_filter";

    public static final String FILTER_ALL = "filter_all";
    public static final String FILTER_MY = "filter_my";

    private static final String KEY_COURSES = "courses";

    private String mFilter;
    private SwipeRefreshLayout mRefreshLayout;

    private AbsListView mAbsListView;
    private CourseListAdapter mCourseListAdapter;

    private NotificationController mNotificationController;

    private List<Course> mCourses;

    private CourseModel mCourseModel;

    private Result<List<Course>> mCourseResult;

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
            mFilter = getArguments().getString(ARG_FILTER);
        }
        if (savedInstanceState != null) {
            mCourses = savedInstanceState.getParcelableArrayList(KEY_COURSES);
        }
        setHasOptionsMenu(true);

        mCourseModel = new CourseModel(getActivity(), jobManager, databaseHelper);
        mCourseResult = new Result<List<Course>>() {
            @Override
            protected void onSuccess(List<Course> result, DataSource dataSource) {
                mCourses = result;
                updateView();
            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                if (warnCode == WarnCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                }
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                if (errorCode == ErrorCode.NO_RESULT) {
                    ToastUtil.show(getActivity(), getActivity().getString(R.string.toast_no_courses)
                            + " " + getActivity().getString(R.string.toast_no_network));
                } else if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                }
            }
        };
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCourses != null) {
            outState.putParcelableArrayList(KEY_COURSES, (ArrayList<Course>) mCourses);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_courses, container, false);

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        RefeshLayoutController.setup(mRefreshLayout, this);

        mCourseListAdapter = new CourseListAdapter(getActivity(), this, mFilter);

        mAbsListView = (AbsListView) layout.findViewById(R.id.listView);
        mAbsListView.setAdapter(mCourseListAdapter);

        mNotificationController = new NotificationController(getActivity(), layout);
        mNotificationController.setNotificationVisible(false);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        mNotificationController.setProgressVisible(true);

        if (mFilter.equals(FILTER_ALL)) {
            mActivityCallback.onFragmentAttached(NavigationAdapter.NAV_ID_ALL_COURSES, getString(R.string.title_section_all_courses));
        } else if (mFilter.equals(FILTER_MY)) {
            mActivityCallback.onFragmentAttached(NavigationAdapter.NAV_ID_MY_COURSES, getString(R.string.title_section_my_courses));
            if (!UserModel.isLoggedIn(getActivity())) {
                mNotificationController.setProgressVisible(false);
                mNotificationController.setTitle(R.string.notification_please_login);
                mNotificationController.setSummary(R.string.notification_please_login_summary);
                mNotificationController.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
                    }
                });
                mNotificationController.setNotificationVisible(true);
            }
        }

        if (mCourses != null) {
            mCourseListAdapter.updateCourses(mCourses);
            mRefreshLayout.setRefreshing(false);
            mNotificationController.setProgressVisible(false);
            mNotificationController.setNotificationVisible(false);
        } else {
            mRefreshLayout.setRefreshing(true);
            mCourseModel.getCourses(mCourseResult, false);
        }
    }

    private void updateView() {
        if (isAdded()) {
            mRefreshLayout.setRefreshing(false);
            mNotificationController.setProgressVisible(false);
            if (mFilter.equals(FILTER_MY)) {
                if (!UserModel.isLoggedIn(getActivity())) {
                    mNotificationController.setTitle(R.string.notification_please_login);
                    mNotificationController.setSummary(R.string.notification_please_login_summary);
                    mNotificationController.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
                        }
                    });
                    mNotificationController.setNotificationVisible(true);
                } else if (mCourses.size() == 0) {
                    mNotificationController.setTitle(R.string.notification_no_enrollments);
                    mNotificationController.setSummary(R.string.notification_no_enrollments_summary);
                    mNotificationController.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_ALL_COURSES);
                        }
                    });
                    mNotificationController.setNotificationVisible(true);
                } else {
                    mNotificationController.setNotificationVisible(false);
                }
            }
            if (mCourses != null) {
                mCourseListAdapter.updateCourses(mCourses);
            }
            mActivityCallback.updateDrawer();
        }
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        mCourseModel.getCourses(mCourseResult, false);
    }

    @Override
    public void onEnrollButtonClicked(Course course) {
        Result<Void> result = new Result<Void>() {
            @Override
            protected void onSuccess(Void result, DataSource dataSource) {
                mCourseModel.getCourses(mCourseResult, false);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                } else if (errorCode == ErrorCode.NO_AUTH) {
                    ToastUtil.show(getActivity(), R.string.toast_please_log_in);
                    mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
                }
            }
        };
        mCourseModel.addEnrollment(result, course);
    }

    @Override
    public void onEnterButtonClicked(Course course) {
        if (UserModel.isLoggedIn(getActivity())) {
            Intent intent = new Intent(getActivity(), CourseActivity.class);
            Bundle b = new Bundle();
            b.putParcelable(CourseActivity.ARG_COURSE, course);
            intent.putExtras(b);
            startActivity(intent);
        } else {
            ToastUtil.show(getActivity(), R.string.toast_please_log_in);
            mActivityCallback.selectDrawerSection(NavigationAdapter.NAV_ID_PROFILE);
        }
    }

    @Override
    public void onDetailButtonClicked(Course course) {
        Intent intent = new Intent(getActivity(), CourseDetailsActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(CourseActivity.ARG_COURSE, course);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mActivityCallback.isDrawerOpen())
            inflater.inflate(R.menu.refresh, menu);
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
