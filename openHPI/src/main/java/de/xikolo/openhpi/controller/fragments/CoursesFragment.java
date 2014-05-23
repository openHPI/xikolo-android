package de.xikolo.openhpi.controller.fragments;

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

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.controller.adapter.CoursesListAdapter;
import de.xikolo.openhpi.manager.CoursesManager;
import de.xikolo.openhpi.model.Course;

public class CoursesFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = CoursesFragment.class.getSimpleName();

    private static final String KEY_COURSES = "courses";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;

    private SwipeRefreshLayout mRefreshLayout;
    private AbsListView mAbsListView;
    private CoursesListAdapter mCoursesListAdapter;

    private CoursesManager mCoursesManager;

    private List<Course> mCourses;

    public CoursesFragment() {
        // Required empty public constructor
    }

    public static CoursesFragment newInstance() {
        CoursesFragment fragment = new CoursesFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCallback.onFragmentAttached(0);
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
        mRefreshLayout.setColorScheme(
                R.color.red,
                R.color.orange,
                R.color.red,
                R.color.orange);
        mRefreshLayout.setOnRefreshListener(this);

        mCoursesListAdapter = new CoursesListAdapter(getActivity());

        mAbsListView = (AbsListView) layout.findViewById(R.id.listView);
        mAbsListView.setAdapter(mCoursesListAdapter);

        mCoursesManager = new CoursesManager(getActivity()) {
            @Override
            public void onCoursesRequestReceived(List<Course> courses) {
                mRefreshLayout.setRefreshing(false);
                mCourses = courses;
                mCoursesListAdapter.update(courses);
            }

            @Override
            public void onCoursesRequestCancelled() {
                mRefreshLayout.setRefreshing(false);
            }
        };

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_COURSES)) {
            mCourses = savedInstanceState.getParcelableArrayList(KEY_COURSES);
            mCoursesListAdapter.update(mCourses);
        }

        if (mCourses == null) {
            mRefreshLayout.setRefreshing(true);
            mCoursesManager.requestCourses(true);
        }

        return layout;
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        mCoursesManager.requestCourses(false);
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
