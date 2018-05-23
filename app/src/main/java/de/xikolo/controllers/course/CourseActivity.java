package de.xikolo.controllers.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

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
import de.xikolo.controllers.helper.CourseArea;
import de.xikolo.controllers.login.LoginActivityAutoBundle;
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle;
import de.xikolo.events.NetworkStateEvent;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.CoursePresenter;
import de.xikolo.presenters.course.CoursePresenterFactory;
import de.xikolo.presenters.course.CourseView;
import de.xikolo.utils.ShareUtil;
import de.xikolo.utils.ToastUtil;

public class CourseActivity extends BasePresenterActivity<CoursePresenter, CourseView> implements CourseView, UnenrollDialog.Listener {

    public static final String TAG = CourseActivity.class.getSimpleName();

    @AutoBundleField String courseId;

    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.stub_bottom) ViewStub stubBottom;

    ProgressDialog progressDialog;

    CoursePagerAdapter adapter;

    View enrollBar;
    Button enrollButton;

    private boolean enrollable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_tabs);
        setupActionBar(false);
        enableOfflineModeToolbar(true);
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
                    Intent restartIntent = CourseActivityAutoBundle.builder(courseId).build(this);
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

        if (stubBottom.getParent() != null) {
            stubBottom.setLayoutResource(R.layout.content_enroll_button);
            enrollBar = stubBottom.inflate();
            enrollButton = enrollBar.findViewById(R.id.button_enroll);
            enrollButton.setOnClickListener((v) -> presenter.enroll());
        }

        courseId = course.id;
        initAdapter();
        viewPager.setOffscreenPageLimit(2);

        // Bind the tabs to the ViewPager
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(courseTab);

        setEnrollmentFunctionsAvailable(true);
        hideEnrollBar();
    }

    private void initAdapter() {
        adapter = new CoursePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.addOnTabSelectedListener(adapter);
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

        if (!enrollable)
            inflater.inflate(R.menu.unenroll, menu);

        inflater.inflate(R.menu.share, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkStateEvent event) {
        super.onNetworkEvent(event);

        if (appBarLayout != null) {
            if (event.isOnline()) {
                toolbar.setSubtitle("");
                tabLayout.setBackgroundColor(getResources().getColor(R.color.apptheme_toolbar));
                setColorScheme(R.color.apptheme_toolbar, R.color.apptheme_statusbar);
            } else {
                toolbar.setSubtitle(getString(R.string.offline_mode));
                tabLayout.setBackgroundColor(getResources().getColor(R.color.offline_mode_toolbar));
                setColorScheme(R.color.offline_mode_toolbar, R.color.offline_mode_statusbar);
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
            case R.id.action_share:
                ShareUtil.shareCourseLink(this, courseId);
                return true;
            case R.id.action_unenroll:
                UnenrollDialog dialog = new UnenrollDialog();
                dialog.setListener(this);
                dialog.show(getSupportFragmentManager(), UnenrollDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        presenter.unenroll();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.getItem(viewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialogAutoBundle.builder().build();
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
    public void showLoginRequiredMessage() {
        ToastUtil.show(R.string.toast_please_log_in);
    }

    @Override
    public void openLogin() {
        Intent intent = LoginActivityAutoBundle.builder().build(this);
        startActivity(intent);
    }

    @Override
    public void setEnrollmentFunctionsAvailable(boolean available) {
        initAdapter();
        if (available)
            adapter.setHiding(false);
        else
            adapter.setHiding(true);

        adapter.notifyDataSetChanged();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void hideEnrollBar() {
        this.enrollable = false;
        if (enrollBar != null)
            enrollBar.setVisibility(View.GONE);
    }

    @Override
    public void showEnrollOption() {
        this.enrollable = true;
        if (enrollBar != null && enrollButton != null) {
            enrollBar.setVisibility(View.VISIBLE);
            enrollButton.setEnabled(true);
            enrollButton.setClickable(true);
            enrollButton.setText(R.string.btn_enroll);
        }
    }

    @Override
    public void showCourseStartsSoon() {
        this.enrollable = false;
        if (enrollBar != null && enrollButton != null) {
            enrollBar.setVisibility(View.VISIBLE);
            enrollButton.setEnabled(false);
            enrollButton.setClickable(false);
            enrollButton.setText(R.string.btn_starts_soon);
        }
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void restartActivity() {
        Intent i = getIntent();
        finish();
        startActivity(i);
    }

    @NonNull
    @Override
    protected PresenterFactory<CoursePresenter> getPresenterFactory() {
        return new CoursePresenterFactory();
    }

    public class CoursePagerAdapter extends FragmentPagerAdapter implements TabLayout.OnTabSelectedListener {

        private List<String> getTitles(boolean hide) {
            List<String> titles = new ArrayList<>();
            if (!hide) {
                titles.add(getString(R.string.tab_learnings));
                titles.add(getString(R.string.tab_discussions));
                titles.add(getString(R.string.tab_progress));
            }
            titles.add(getString(R.string.tab_course_details));
            titles.add(getString(R.string.tab_course_certificates));

            if (!hide) {
                titles.add(getString(R.string.tab_announcements));

                if (FeatureToggle.recapMode()) {
                    titles.add(getString(R.string.tab_recap));
                }
            }

            return titles;
        }

        private FragmentManager fragmentManager;

        private List<String> TITLES;

        private boolean hiding = false;

        public CoursePagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentManager = fm;
            setHiding(false);
        }

        public void setHiding(boolean hide) {
            hiding = hide;
            TITLES = getTitles(hide);
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
            //in case some items are hidden
            if (hiding) {
                position += 3;
            }

            // Check if this Fragment already exists.
            // Fragment Name is saved by FragmentPagerAdapter implementation.
            String name = makeFragmentName(R.id.viewpager, position);
            Fragment fragment = fragmentManager.findFragmentByTag(name);
            if (fragment == null) {
                switch (CourseArea.get(position)) {
                    case LEARNINGS:
                        fragment = LearningsFragmentAutoBundle.builder(courseId).build();
                        break;
                    case DISCUSSIONS:
                        fragment = WebViewFragmentAutoBundle.builder(Config.HOST_URL + Config.COURSES + courseId + "/" + Config.DISCUSSIONS)
                                .inAppLinksEnabled(true)
                                .externalLinksEnabled(false)
                                .build();
                        break;
                    case PROGRESS:
                        fragment = ProgressFragmentAutoBundle.builder(courseId).build();
                        break;
                    case COURSE_DETAILS:
                        fragment = DescriptionFragmentAutoBundle.builder(courseId).build();
                        break;
                    case DOCUMENTS:
                        fragment = DocumentListFragmentAutoBundle.builder(courseId).build();
                        break;
                    case ANNOUNCEMENTS:
                        fragment = AnnouncementListFragmentAutoBundle.builder(courseId).build();
                        break;
                    case RECAP:
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
