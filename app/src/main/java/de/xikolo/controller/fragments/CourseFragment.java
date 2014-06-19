package de.xikolo.controller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.fragments.dialog.UnenrollDialog;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.manager.EnrollmentsManager;
import de.xikolo.model.Course;
import de.xikolo.model.Enrollment;
import de.xikolo.util.Config;
import eu.inmite.android.lib.dialogs.ISimpleDialogListener;

public class CourseFragment extends ContentFragment implements ISimpleDialogListener {

    public final static String TAG = CourseFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private Course mCourse;

    public CourseFragment() {
        // Required empty public constructor
    }

    public static CourseFragment newInstance(Course course) {
        CourseFragment fragment = new CourseFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COURSE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = getArguments().getParcelable(ARG_COURSE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCallback.onLowLevelFragmentAttached(NavigationAdapter.NAV_ID_LOW_LEVEL_CONTENT, mCourse.name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialize the ViewPager and set an adapter
        View layout = inflater.inflate(R.layout.fragment_course, container, false);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) layout.findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) layout.findViewById(R.id.pager);
        pager.setAdapter(new CoursePagerAdapter(getChildFragmentManager()));

        // Bind the tabs to the ViewPager
        tabs.setViewPager(pager);

        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mCallback.isDrawerOpen())
            inflater.inflate(R.menu.course, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_unenroll:
                UnenrollDialog.show(getActivity(), getChildFragmentManager(), this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButtonClicked(int i) {
        EnrollmentsManager manager = new EnrollmentsManager(getActivity()) {
            @Override
            public void onEnrollmentsRequestReceived(List<Enrollment> enrolls) {}

            @Override
            public void onEnrollmentsRequestCancelled() {}
        };
        manager.deleteEnrollment(mCourse.id);
        mCallback.toggleDrawer(NavigationAdapter.NAV_ID_MY_COURSES);
    }

    @Override
    public void onNegativeButtonClicked(int i) {}

    public class CoursePagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {
                getString(R.string.tab_learnings),
                getString(R.string.tab_discussions),
                getString(R.string.tab_progress),
                getString(R.string.tab_announcements),
                getString(R.string.tab_rooms),
                getString(R.string.tab_details)
        };

        public CoursePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = CourseLearningsFragment.newInstance(mCourse);
                    break;
                case 1:
                    fragment = WebViewFragment.newInstance(Config.URI_SAP + Config.PATH_COURSES + mCourse.id + "/" + Config.PATH_DISCUSSIONS, false, null);
                    break;
                case 2:
                    fragment = ModuleFragment.newInstance();
                    break;
                case 3:
                    fragment = WebViewFragment.newInstance(Config.URI_SAP + Config.PATH_COURSES + mCourse.id + "/" + Config.PATH_ANNOUNCEMENTS, false, null);
                    break;
                case 4:
                    fragment = WebViewFragment.newInstance(Config.URI_SAP + Config.PATH_COURSES + mCourse.id + "/" + Config.PATH_ROOMS, false, null);
                    break;
                case 5:
                    fragment = WebViewFragment.newInstance(mCourse.url, false, null);
                    break;
            }
            return fragment;
        }

    }

}
