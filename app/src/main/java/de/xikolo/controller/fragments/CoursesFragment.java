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
import de.xikolo.controller.fragments.adapter.CoursesListAdapter;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.manager.CoursesManager;
import de.xikolo.manager.EnrollmentsManager;
import de.xikolo.manager.TokenManager;
import de.xikolo.model.Course;
import de.xikolo.model.Enrollment;

public class CoursesFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener, CoursesListAdapter.OnEnrollButtonClickListener {

    public static final String TAG = CoursesFragment.class.getSimpleName();

    private static final String KEY_COURSES = "courses";
    private static final String KEY_ENROLLMENTS = "enrollments";

    private SwipeRefreshLayout mRefreshLayout;
    private AbsListView mAbsListView;
    private CoursesListAdapter mCoursesListAdapter;

    private CoursesManager mCoursesManager;
    private EnrollmentsManager mEnrollmentsManager;

    private List<Course> mCourses;
    private List<Enrollment> mEnrollments;

    public CoursesFragment() {
        // Required empty public constructor
    }

    public static CoursesFragment newInstance() {
        CoursesFragment fragment = new CoursesFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_ALL_COURSES, getString(R.string.title_section_all_courses));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCourses != null) {
            outState.putParcelableArrayList(KEY_COURSES, (ArrayList<Course>) mCourses);
            if (TokenManager.isLoggedIn(getActivity())) {
                outState.putParcelableArrayList(KEY_ENROLLMENTS, (ArrayList<Enrollment>) mEnrollments);
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_courses, container, false);

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorScheme(
                R.color.red,
                R.color.orange,
                R.color.red,
                R.color.orange);
        mRefreshLayout.setOnRefreshListener(this);

        mCoursesListAdapter = new CoursesListAdapter(getActivity(), this);

        mAbsListView = (AbsListView) layout.findViewById(R.id.listView);
        mAbsListView.setAdapter(mCoursesListAdapter);

        mCoursesManager = new CoursesManager(getActivity()) {
            @Override
            public void onCoursesRequestReceived(List<Course> courses) {
                mRefreshLayout.setRefreshing(false);
                mCourses = courses;
                mCoursesListAdapter.updateCourses(courses);
            }

            @Override
            public void onCoursesRequestCancelled() {
                mRefreshLayout.setRefreshing(false);
            }
        };

        mEnrollmentsManager = new EnrollmentsManager(getActivity()) {
            @Override
            public void onEnrollmentsRequestReceived(List<Enrollment> enrolls) {
                mEnrollments = enrolls;
                mCoursesListAdapter.updateEnrollments(enrolls);
            }

            @Override
            public void onEnrollmentsRequestCancelled() {
            }
        };

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_COURSES)) {
            mCourses = savedInstanceState.getParcelableArrayList(KEY_COURSES);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ENROLLMENTS) && TokenManager.isLoggedIn(getActivity())) {
            mEnrollments = savedInstanceState.getParcelableArrayList(KEY_ENROLLMENTS);
        }

        if (mEnrollments == null && TokenManager.isLoggedIn(getActivity())) {
            mEnrollmentsManager.requestEnrollments(true);
        } else {
            mCoursesListAdapter.updateEnrollments(mEnrollments);
        }

        if (mCourses == null) {
            mRefreshLayout.setRefreshing(true);
            mCoursesManager.requestCourses(true);
        } else {
            mCoursesListAdapter.updateCourses(mCourses);
        }

        return layout;
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        if (TokenManager.isLoggedIn(getActivity())) {
            mEnrollmentsManager.requestEnrollments(false);
        }
        mCoursesManager.requestCourses(false);
    }

    @Override
    public void onEnrollButtonClicked(String id) {
        if (TokenManager.isLoggedIn(getActivity()))
            mEnrollmentsManager.createEnrollment(id);
        else
            mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
    }

    @Override
    public void onEnterButtonClicked(String id) {
        if (TokenManager.isLoggedIn(getActivity()))
            mEnrollmentsManager.deleteEnrollment(id);
        else
            mCallback.toggleDrawer(NavigationAdapter.NAV_ID_PROFILE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mCallback.isDrawerOpen())
            inflater.inflate(R.menu.webview, menu);
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
