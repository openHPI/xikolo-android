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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.CourseActivity;
import de.xikolo.controller.CourseDetailsActivity;
import de.xikolo.controller.main.adapter.CourseListAdapter;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.OnModelResponseListener;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class CourseListFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener,
        CourseListAdapter.OnCourseButtonClickListener {

    public static final String TAG = CourseListFragment.class.getSimpleName();

    public static final String ARG_FILTER = "arg_filter";

    private static final String KEY_COURSES = "courses";

    private String mFilter;
    private SwipeRefreshLayout mRefreshLayout;
    private AbsListView mAbsListView;
    private CourseListAdapter mCourseListAdapter;

    private View mNotification;
    private TextView mTextNotification;

    private List<Course> mCourses;

    private CourseModel mCourseModel;

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

        mCourseModel = new CourseModel(getActivity(), jobManager);
        mCourseModel.setRetrieveCoursesListener(new OnModelResponseListener<List<Course>>() {
            @Override
            public void onResponse(final List<Course> response) {
                if (response != null) {
                    mCourses = response;
                    updateView();
                } else {
                    mRefreshLayout.setRefreshing(false);
                    ToastUtil.show(getActivity(), getActivity().getString(R.string.toast_no_courses)
                            + " " + getActivity().getString(R.string.toast_no_network));
                }
            }
        });
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

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorSchemeResources(
                R.color.apptheme_second,
                R.color.apptheme_main,
                R.color.apptheme_second,
                R.color.apptheme_main);
        mRefreshLayout.setOnRefreshListener(this);

        mCourseListAdapter = new CourseListAdapter(getActivity(), this);

        mAbsListView = (AbsListView) layout.findViewById(R.id.listView);
        mAbsListView.setAdapter(mCourseListAdapter);

        mNotification = layout.findViewById(R.id.containerNotification);
        mTextNotification = (TextView) layout.findViewById(R.id.textNotification);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mFilter.equals(CourseModel.FILTER_ALL)) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_ALL_COURSES, getString(R.string.title_section_all_courses));
        } else if (mFilter.equals(CourseModel.FILTER_MY)) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_MY_COURSES, getString(R.string.title_section_my_courses));
            if (!UserModel.isLoggedIn(getActivity())) {
                mNotification.setVisibility(View.VISIBLE);
                mTextNotification.setText(getString(R.string.notification_please_login));
                mNotification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
                    }
                });
            }
        }

        if (mCourses != null) {
            mCourseListAdapter.updateCourses(mCourses);
            mRefreshLayout.setRefreshing(false);
        } else {
            if (NetworkUtil.isOnline(getActivity())) {
                mRefreshLayout.setRefreshing(true);
                mCourseModel.retrieveCourses(mFilter, true, false);
            } else {
                NetworkUtil.showNoConnectionToast(getActivity());
            }
        }
    }

    private void updateView() {
        mRefreshLayout.setRefreshing(false);
        if (mFilter.equals(CourseModel.FILTER_MY)) {
            if (mCourses.size() == 0) {
                mNotification.setVisibility(View.VISIBLE);
                mTextNotification.setText(getString(R.string.notification_no_enrollments));
                mNotification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallback.toggleDrawer(NavigationAdapter.NAV_ID_ALL_COURSES);
                    }
                });
            } else {
                mNotification.setVisibility(View.GONE);
            }
        }
        if (mCourses != null) {
            mCourseListAdapter.updateCourses(mCourses);
        }
        mCallback.updateDrawer();
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        if (NetworkUtil.isOnline(getActivity())) {
            mCourseModel.retrieveCourses(mFilter, false, false);
        } else {
            mRefreshLayout.setRefreshing(false);
            NetworkUtil.showNoConnectionToast(getActivity());
        }
    }

    @Override
    public void onEnrollButtonClicked(Course course) {
        if (UserModel.isLoggedIn(getActivity())) {
            if (NetworkUtil.isOnline(getActivity())) {
                mCourseModel.createEnrollment(course.id);
            } else {
                NetworkUtil.showNoConnectionToast(getActivity());
            }
        } else {
            ToastUtil.show(getActivity(), R.string.toast_please_log_in);
            mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
        }
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
            mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
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
