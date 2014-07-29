package de.xikolo.controller.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.fragments.adapter.CourseListAdapter;
import de.xikolo.controller.fragments.adapter.FilteredCourseListAdapter;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.manager.CourseManager;
import de.xikolo.manager.EnrollmentsManager;
import de.xikolo.manager.TokenManager;
import de.xikolo.model.Course;
import de.xikolo.model.Enrollment;
import de.xikolo.util.Network;
import de.xikolo.util.Toaster;

public class CourseListFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener,
        CourseListAdapter.OnCourseButtonClickListener {

    public static final String TAG = CourseListFragment.class.getSimpleName();

    public static final String ARG_FILTER = "arg_filter";
    public static final String FILTER_ALL = "filter_all";
    public static final String FILTER_MY = "filter_my";

    private static final String KEY_COURSES = "courses";
    private static final String KEY_ENROLLMENTS = "enrollments";

    private String mFilter;
    private SwipeRefreshLayout mRefreshLayout;
    private AbsListView mAbsListView;
    private CourseListAdapter mCourseListAdapter;

    private CourseManager mCourseManager;
    private EnrollmentsManager mEnrollmentsManager;

    private List<Course> mCourses;
    private List<Enrollment> mEnrollments;

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
            mEnrollments = savedInstanceState.getParcelableArrayList(KEY_ENROLLMENTS);
        }
        setHasOptionsMenu(true);

        if (mFilter.equals(FILTER_MY)) {
            if (!TokenManager.isLoggedIn(getActivity())) {
                Toaster.show(getActivity(), R.string.toast_please_log_in);
                mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCourses != null) {
            outState.putParcelableArrayList(KEY_COURSES, (ArrayList<Course>) mCourses);
        }
        if (mEnrollments != null) {
            outState.putParcelableArrayList(KEY_ENROLLMENTS, (ArrayList<Enrollment>) mEnrollments);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_courses, container, false);

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorSchemeResources(
                R.color.red,
                R.color.orange,
                R.color.red,
                R.color.orange);
        mRefreshLayout.setOnRefreshListener(this);

        mCourseListAdapter = new FilteredCourseListAdapter(getActivity(), this, mFilter);

        mAbsListView = (AbsListView) layout.findViewById(R.id.listView);
        mAbsListView.setAdapter(mCourseListAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mFilter.equals(FILTER_ALL)) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_ALL_COURSES, getString(R.string.title_section_all_courses));
        } else if (mFilter.equals(FILTER_MY)) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_MY_COURSES, getString(R.string.title_section_my_courses));
        }

        mCourseManager = new CourseManager(getActivity()) {
            @Override
            public void onCoursesRequestReceived(List<Course> courses) {
                mRefreshLayout.setRefreshing(false);
                if (courses != null) {
                    mCourses = courses;
                    mCourseListAdapter.updateCourses(courses);
                }
            }

            @Override
            public void onCoursesRequestCancelled() {
                mRefreshLayout.setRefreshing(false);
            }
        };

        mEnrollmentsManager = new EnrollmentsManager(getActivity()) {
            @Override
            public void onEnrollmentsRequestReceived(List<Enrollment> enrolls) {
                if (enrolls != null) {
                    mEnrollments = enrolls;
                    mCourseListAdapter.updateEnrollments(enrolls);
                }
            }

            @Override
            public void onEnrollmentsRequestCancelled() {
            }
        };

        if (Network.isOnline(getActivity())) {
            if (TokenManager.isLoggedIn(getActivity())) {
                if (mEnrollments == null) {
                    mEnrollmentsManager.requestEnrollments(true);
                } else {
                    mCourseListAdapter.updateEnrollments(mEnrollments);
                }
            }

            if (mCourses == null) {
                mRefreshLayout.setRefreshing(true);
                mCourseManager.requestCourses(true);
            } else {
                mCourseListAdapter.updateCourses(mCourses);
            }
        } else {
            Network.showNoConnectionToast(getActivity());
        }
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        if (Network.isOnline(getActivity())) {
            if (TokenManager.isLoggedIn(getActivity())) {
                mEnrollmentsManager.requestEnrollments(false);
            }
            mCourseManager.requestCourses(false);
        } else {
            mRefreshLayout.setRefreshing(false);
            Network.showNoConnectionToast(getActivity());
        }
    }

    @Override
    public void onEnrollButtonClicked(Course course) {
        if (TokenManager.isLoggedIn(getActivity())) {
            mEnrollmentsManager.createEnrollment(course.id);
        } else {
            Toaster.show(getActivity(), R.string.toast_please_log_in);
            mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
        }
    }

    @Override
    public void onEnterButtonClicked(Course course) {
        if (TokenManager.isLoggedIn(getActivity())) {
            mCallback.attachFragment(CourseFragment.newInstance(course));
        } else {
            Toaster.show(getActivity(), R.string.toast_please_log_in);
            mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
        }
    }

    @Override
    public void onDetailButtonClicked(Course course) {
        mCallback.attachFragment(WebViewFragment.newInstance(course.url, false, course.name));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mCallback.isDrawerOpen())
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
