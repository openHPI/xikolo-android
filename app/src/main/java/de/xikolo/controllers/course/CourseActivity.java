package de.xikolo.controllers.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.yatatsu.autobundle.AutoBundleField;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.config.FeatureToggle;
import de.xikolo.controllers.base.BasePresenterActivity;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.controllers.dialogs.UnenrollDialog;
import de.xikolo.controllers.helper.CacheHelper;
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle;
import de.xikolo.events.NetworkStateEvent;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.CoursePresenter;
import de.xikolo.presenters.course.CoursePresenterFactory;
import de.xikolo.presenters.course.CourseView;
import de.xikolo.utils.ToastUtil;

public class CourseActivity extends BasePresenterActivity<CoursePresenter, CourseView> implements CourseView, UnenrollDialog.UnenrollDialogListener {

    public static final String TAG = CourseActivity.class.getSimpleName();

    @AutoBundleField(required = false) String courseId;

    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;

    ProgressDialog progressDialog;

    CoursePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_tabs);
        setupActionBar();
    }

    @Override
    protected void onPresenterCreatedOrRestored(@NonNull CoursePresenter presenter) {
        String action = getIntent().getAction();

        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            presenter.handleDeepLink(getIntent().getData());
        } else {
            if (courseId == null) {
                CacheHelper cacheController = new CacheHelper();
                cacheController.readCachedExtras();
                if (cacheController.getCourse() != null) {
                    courseId = cacheController.getCourse().id;
                }
                if (courseId != null) {
                    Intent restartIntent = CourseActivityAutoBundle.builder().courseId(courseId).build(this);
                    finish();
                    startActivity(restartIntent);
                }
            } else {
                presenter.initCourse(courseId);
            }
        }
    }

    @Override
    public void setupView(Course course, int courseTab) {
        setTitle(course.title);
        courseId = course.id;
        adapter = new CoursePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        // Bind the tabs to the ViewPager
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(adapter);

        viewPager.setCurrentItem(courseTab);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            String action = intent.getAction();

            if (action != null && action.equals(Intent.ACTION_VIEW)) {
                presenter.handleDeepLink(intent.getData());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.unenroll, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkStateEvent event) {
        super.onNetworkEvent(event);

        if (tabLayout != null) {
            if (event.isOnline()) {
                tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.apptheme_toolbar));
            } else {
                tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.offline_mode_toolbar));
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
        presenter.unenroll(courseId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.getItem(viewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.getInstance();
        }
        progressDialog.show(getSupportFragmentManager(), ProgressDialog.TAG);
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.getDialog() != null && progressDialog.getDialog().isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showErrorToast() {
        ToastUtil.show(R.string.error);
    }

    @Override
    public void showNoNetworkToast() {
        ToastUtil.show(R.string.toast_no_network);
    }

    @Override
    public void showNotEnrolledToast() {
        ToastUtil.show(R.string.notification_not_enrolled);
    }

    @Override
    public void showCourseLockedToast() {
        ToastUtil.show(R.string.notification_course_locked);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void startCourseDetailsActivity(String courseId) {
        Intent intent = CourseDetailsActivityAutoBundle.builder(courseId).build(this);
        startActivity(intent);
    }

    @NonNull
    @Override
    protected PresenterFactory<CoursePresenter> getPresenterFactory() {
        return new CoursePresenterFactory();
    }

    public class CoursePagerAdapter extends FragmentPagerAdapter implements TabLayout.OnTabSelectedListener {

        private final List<String> TITLES;

        {
            TITLES = new ArrayList<>();
            TITLES.add(getString(R.string.tab_learnings));
            TITLES.add(getString(R.string.tab_discussions));
            TITLES.add(getString(R.string.tab_progress));
            TITLES.add(getString(R.string.tab_collab_space));
            TITLES.add(getString(R.string.tab_course_details));
            TITLES.add(getString(R.string.tab_announcements));

            if (FeatureToggle.recapMode()) {
                TITLES.add(getString(R.string.tab_recap));
            }
        }

        private FragmentManager fragmentManager;

        public CoursePagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentManager = fm;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES.get(position);
        }

        @Override
        public int getCount() {
            return TITLES.size();
        }

        @Override
        public Fragment getItem(int position) {
            // Check if this Fragment already exists.
            // Fragment Name is saved by FragmentPagerAdapter implementation.
            String name = makeFragmentName(R.id.viewpager, position);
            Fragment fragment = fragmentManager.findFragmentByTag(name);
            if (fragment == null) {
                switch (position) {
                    case Course.TAB_LEARNINGS:
                        fragment = LearningsFragmentAutoBundle.builder(courseId).build();
                        break;
                    case Course.TAB_DISCUSSIONS:
                        fragment = WebViewFragmentAutoBundle.builder(Config.HOST_URL + Config.COURSES + courseId + "/" + Config.DISCUSSIONS)
                                .inAppLinksEnabled(true)
                                .externalLinksEnabled(false)
                                .build();
                        break;
                    case Course.TAB_PROGRESS:
                        fragment = ProgressFragmentAutoBundle.builder(courseId).build();
                        break;
                    case Course.TAB_COLLAB_SPACE:
                        fragment = WebViewFragmentAutoBundle.builder(Config.HOST_URL + Config.COURSES + courseId + "/" + Config.COLLAB_SPACE)
                                .inAppLinksEnabled(true)
                                .externalLinksEnabled(false)
                                .build();
                        break;
                    case Course.TAB_COURSE_DETAILS:
                        fragment = CourseDetailsFragmentAutoBundle.builder(courseId).build();
                        break;
                    case Course.TAB_ANNOUNCEMENTS:
                        fragment = AnnouncementListFragmentAutoBundle.builder(courseId).build();
                        break;
                    case Course.TAB_RECAP:
                        fragment = WebViewFragmentAutoBundle.builder(Config.HOST_URL + Config.RECAP + courseId)
                                .inAppLinksEnabled(true)
                                .externalLinksEnabled(false)
                                .build();
                        break;
                }
            }
            return fragment;
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int tabPosition = tabLayout.getSelectedTabPosition();
            viewPager.setCurrentItem(tabPosition, true);
            presenter.setCourseTab(tabPosition);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

}
