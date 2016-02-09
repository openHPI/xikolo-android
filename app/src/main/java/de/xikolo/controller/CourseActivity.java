package de.xikolo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.course.CourseFragment;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.helper.CacheController;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.DeepLinkingUtil;
import de.xikolo.util.ToastUtil;

public class CourseActivity extends BaseActivity {

    public static final String TAG = CourseActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";

    private Course mCourse;
    private int firstFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        setupActionBar();
        setActionBarElevation(0);

        final Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();

            if (action != null && action.equals(Intent.ACTION_VIEW)) {
                handleDeepLinkIntent(intent);
            } else {
                Bundle b = getIntent().getExtras();
                if (b == null || !b.containsKey(ARG_COURSE)) {
                    if (videoCastManager.isConnected()) {
                        CacheController cacheController = new CacheController();
                        cacheController.readCachedExtras();
                        if (cacheController.getCourse() != null) {
                            mCourse = cacheController.getCourse();
                        }
                    } else {
                        throw new WrongParameterException();
                    }
                } else {
                    this.mCourse = b.getParcelable(ARG_COURSE);
                }

                handleCourseData();
            }
        }
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
                    for (Course course : result) {
                        if (course.course_code.equals(courseIntent)) {
                            mCourse = course;
                            if (mCourse.locked || !mCourse.is_enrolled) {
                                setTitle(mCourse.name);

                                String tag = mCourse.name;

                                if (mCourse.locked) {
                                    ToastUtil.show(R.string.notification_course_locked);
                                } else if (!mCourse.is_enrolled) {
                                    ToastUtil.show(R.string.notification_not_enrolled);
                                }

                                FragmentManager fragmentManager = getSupportFragmentManager();
                                if (fragmentManager.findFragmentByTag(tag) == null) {
                                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                                    transaction.replace(R.id.content, WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code, false, false), tag);
                                    transaction.commit();
                                }
                            } else {
                                DeepLinkingUtil.CourseTab courseTab = DeepLinkingUtil.getTab(data.getPath());

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

                                handleCourseData();
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

    private void handleCourseData() {
        setTitle(mCourse.name);

        String tag = mCourse.name;

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, CourseFragment.newInstance(mCourse, firstFragment), tag);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }
}
