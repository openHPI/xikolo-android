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

import java.util.List;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.controller.adapter.CoursesListAdapter;
import de.xikolo.openhpi.manager.CoursesManager;
import de.xikolo.openhpi.model.Course;

public class CoursesFragment extends ContentFragment implements CoursesManager.OnCoursesReceivedListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = CoursesFragment.class.getSimpleName();

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

        mCoursesManager = new CoursesManager(this, getActivity());
        onRefresh();

        return layout;
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        mCoursesManager.requestCourses();
    }

    @Override
    public void onCoursesReceived(List<Course> courses) {
        mRefreshLayout.setRefreshing(false);
        mCoursesListAdapter.update(courses);
    }

    @Override
    public void onCoursesRequestCancelled() {
        mRefreshLayout.setRefreshing(false);
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
