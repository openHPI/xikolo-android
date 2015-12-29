package de.xikolo.controller.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import de.greenrobot.event.EventBus;
import de.xikolo.R;
import de.xikolo.controller.BaseFragment;
import de.xikolo.controller.WebViewFragment;
import de.xikolo.controller.dialogs.UnenrollDialog;
import de.xikolo.controller.helper.EnrollmentController;
import de.xikolo.data.entities.Course;
import de.xikolo.model.events.NetworkStateEvent;
import de.xikolo.util.Config;

public class CourseFragment extends BaseFragment implements UnenrollDialog.UnenrollDialogListener {

    public final static String TAG = CourseFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";
    private static final String ARG_START = "arg_start";

    private Course mCourse;
    private int firstFragment;

    private ViewPager mPager;
    private CoursePagerAdapter mAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;

    public CourseFragment() {
        // Required empty public constructor
    }

    public static CourseFragment newInstance(Course course, int firstFragment) {
        CourseFragment fragment = new CourseFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COURSE, course);
        args.putInt(ARG_START, firstFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = getArguments().getParcelable(ARG_COURSE);
            firstFragment = getArguments().getInt(ARG_START);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_course, container, false);

        // Initialize the ViewPager and set an adapter
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) layout.findViewById(R.id.tabs);
        mPager = (ViewPager) layout.findViewById(R.id.pager);

        mAdapter = new CoursePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(mAdapter.getCount() - 1);

        // Bind the tabs to the ViewPager
        mPagerSlidingTabStrip.setViewPager(mPager);

        mPager.setCurrentItem(firstFragment);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(NetworkStateEvent event) {
        if (mPagerSlidingTabStrip != null) {
            if (event.isOnline()) {
                mPagerSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.apptheme_main));
            } else {
                mPagerSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.offline_mode));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.course_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.action_unenroll:
                UnenrollDialog dialog = new UnenrollDialog();
                dialog.setUnenrollDialogListener(this);
                dialog.show(getFragmentManager(), UnenrollDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EnrollmentController.unenroll(getActivity(), mCourse);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAdapter.getItem(mPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
    }

    public class CoursePagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {
                getString(R.string.tab_learnings),
                getString(R.string.tab_discussions),
                getString(R.string.tab_progress),
                getString(R.string.tab_rooms),
                getString(R.string.tab_announcements),
                getString(R.string.tab_details)
        };
        private FragmentManager mFragmentManager;

        public CoursePagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
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
            // Check if this Fragment already exists.
            // Fragment Name is saved by FragmentPagerAdapter implementation.
            String name = makeFragmentName(R.id.pager, position);
            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            if (fragment == null) {
                switch (position) {
                    case 0:
                        fragment = CourseLearningsFragment.newInstance(mCourse);
                        break;
                    case 1:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code + "/" + Config.DISCUSSIONS, true, false);
                        break;
                    case 2:
                        fragment = ProgressFragment.newInstance(mCourse);
                        break;
                    case 3:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code + "/" + Config.ROOMS, true, false);
                        break;
                    case 4:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code + "/" + Config.ANNOUNCEMENTS, false, false);
                        break;
                    case 5:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code, false, false);
                        break;
                }
            }
            return fragment;
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }

    }

}
