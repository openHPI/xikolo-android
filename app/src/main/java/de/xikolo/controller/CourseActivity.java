package de.xikolo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.course.CourseLearningsFragment;
import de.xikolo.controller.course.ProgressFragment;
import de.xikolo.controller.dialogs.UnenrollDialog;
import de.xikolo.controller.helper.EnrollmentController;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.model.events.NetworkStateEvent;
import de.xikolo.util.Config;
import de.xikolo.util.DeepLinkingUtil;
import de.xikolo.util.ToastUtil;

public class CourseActivity extends BaseActivity implements UnenrollDialog.UnenrollDialogListener {

    public static final String TAG = CourseActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";

    private Course course;

    private ViewPager viewPager;
    private CoursePagerAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_tabs);
        setupActionBar();

        // Initialize the ViewPager and set an adapter
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        final Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();

            if (action != null && action.equals(Intent.ACTION_VIEW)) {
                handleDeepLinkIntent(intent);
            } else {
                Bundle b = getIntent().getExtras();
                this.course = b.getParcelable(ARG_COURSE);
                setupView(0);
            }
        }
    }

    private void setupView(int firstItem) {
        setTitle(course.name);

        adapter = new CoursePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);

        // Bind the tabs to the ViewPager
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(firstItem);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            String action = intent.getAction();

            if (action != null && action.equals(Intent.ACTION_VIEW)) {
                handleDeepLinkIntent(intent);
            }
        }
    }

    private void handleDeepLinkIntent(Intent intent) {
        final Uri data = intent.getData();
        final String courseIntent = DeepLinkingUtil.getCourseIdentifierFromResumeUri(data);

        Result<List<Course>> result = new Result<List<Course>>() {

            @Override
            protected void onSuccess(List<Course> result, DataSource dataSource) {
                super.onSuccess(result, dataSource);

                if (dataSource == DataSource.NETWORK) {
                    for (Course fetchedCourse : result) {
                        if (fetchedCourse.course_code.equals(courseIntent)) {
                            course = fetchedCourse;
                            if (course.locked || !course.is_enrolled) {
                                setTitle(course.name);

                                if (course.locked) {
                                    ToastUtil.show(R.string.notification_course_locked);
                                } else if (!course.is_enrolled) {
                                    ToastUtil.show(R.string.notification_not_enrolled);
                                }

                                Intent intent = new Intent(CourseActivity.this, CourseDetailsActivity.class);
                                Bundle b = new Bundle();
                                b.putParcelable(CourseActivity.ARG_COURSE, course);
                                intent.putExtras(b);
                                startActivity(intent);
                                finish();
                            } else {
                                DeepLinkingUtil.CourseTab courseTab = DeepLinkingUtil.getTab(data.getPath());

                                int firstFragment = 0;
                                if (courseTab != null) {
                                    switch (courseTab) {
                                        case RESUME:
                                            firstFragment = 0;
                                            break;
                                        case PINBOARD:
                                            firstFragment = 1;
                                            break;
                                        case PROGRESS:
                                            firstFragment = 2;
                                            break;
                                        case LEARNING_ROOMS:
                                            firstFragment = 3;
                                            break;
                                        case ANNOUNCEMENTS:
                                            firstFragment = 4;
                                            break;
                                        case DETAILS:
                                            firstFragment = 5;
                                            break;
                                    }
                                }

                                setupView(firstFragment);
                            }
                            break;
                        }
                    }
                }

            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                super.onWarning(warnCode);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                super.onError(errorCode);

                finish();
            }
        };

        CourseModel courseModel = new CourseModel(jobManager);
        courseModel.getCourses(result, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.unenroll, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onEventMainThread(NetworkStateEvent event) {
        super.onEventMainThread(event);

        if (tabLayout != null) {
            if (event.isOnline()) {
                tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.apptheme_main));
            } else {
                tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.offline_mode_actionbar));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_unenroll:
                UnenrollDialog dialog = new UnenrollDialog();
                dialog.setUnenrollDialogListener(this);
                dialog.show(getSupportFragmentManager(), UnenrollDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EnrollmentController.unenroll(this, course);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.getItem(viewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
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
            String name = makeFragmentName(R.id.viewpager, position);
            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            if (fragment == null) {
                switch (position) {
                    case 0:
                        fragment = CourseLearningsFragment.newInstance(course);
                        break;
                    case 1:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + course.course_code + "/" + Config.DISCUSSIONS, true, false);
                        break;
                    case 2:
                        fragment = ProgressFragment.newInstance(course);
                        break;
                    case 3:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + course.course_code + "/" + Config.ROOMS, true, false);
                        break;
                    case 4:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + course.course_code + "/" + Config.ANNOUNCEMENTS, false, false);
                        break;
                    case 5:
                        fragment = WebViewFragment.newInstance(Config.URI + Config.COURSES + course.course_code, false, false);
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
