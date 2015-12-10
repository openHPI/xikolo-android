package de.xikolo.controller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.xikolo.R;
import de.xikolo.controller.course.CourseFragment;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.model.events.PermissionDeniedEvent;
import de.xikolo.model.events.PermissionGrantedEvent;
import de.xikolo.util.Config;
import de.xikolo.util.DeepLinkingUtil;
import de.xikolo.util.ToastUtil;

public class CourseActivity extends BaseActivity {

    public static final String TAG = CourseActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 54;

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

            if (action != null && action == Intent.ACTION_VIEW) {

                final Uri data = intent.getData();
                final String courseIdent = DeepLinkingUtil.getCourseIdentifierFromResumeUri(data);

                Result<List<Course>> result = new Result<List<Course>>() {

                    @Override
                    protected void onSuccess(List<Course> result, DataSource dataSource) {
                        super.onSuccess(result, dataSource);

                        if(dataSource == DataSource.NETWORK || true) {
                            for (Course course : result) {
                                if (course.course_code.equals(courseIdent)) {
                                    mCourse = course;
                                    if (mCourse.locked || !mCourse.is_enrolled) {
                                        setTitle(mCourse.name);

                                        String tag = "details";

                                        if(dataSource == DataSource.NETWORK) {
                                            if(mCourse.locked) {
                                                ToastUtil.show(getApplicationContext(), R.string.notification_course_locked);
                                            } else if(!mCourse.is_enrolled) {
                                                ToastUtil.show(getApplicationContext(), R.string.notification_not_enrolled);
                                            }
                                        }

                                        FragmentManager fragmentManager = getSupportFragmentManager();
                                        if (fragmentManager.findFragmentByTag(tag) == null) {
                                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.content, WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code, false, false), tag);
                                            transaction.commitAllowingStateLoss();
                                        }
                                    } else {

                                        DeepLinkingUtil.CourseTab courseTab = DeepLinkingUtil.getTab(data.getPath());

                                        if(courseTab != null) {
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
            } else {
                Bundle b = getIntent().getExtras();
                if (b == null || !b.containsKey(ARG_COURSE)) {
                    throw new WrongParameterException();
                } else {
                    this.mCourse = b.getParcelable(ARG_COURSE);
                }

                handleCourseData();
            }
        }
    }

    private void handleCourseData() {
        setTitle(mCourse.name);

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, CourseFragment.newInstance(mCourse, firstFragment), tag);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(Config.DEBUG) {
                        Log.i(TAG, "Permission granted");
                    }
                    EventBus.getDefault().post(new PermissionGrantedEvent(requestCode));
                } else if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    if(Config.DEBUG){
                        Log.i(TAG, "Permission denied");
                    }
                    EventBus.getDefault().post(new PermissionDeniedEvent(requestCode));
                } else {
                    // maybe cancelled?
                }
                return;
            }
            //other permissions in the future
        }
    }

}
