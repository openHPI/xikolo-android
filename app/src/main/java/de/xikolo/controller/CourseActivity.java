package de.xikolo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.course.CourseFragment;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.DeepLinkingUtil;

public class CourseActivity extends BaseActivity {

    public static final String TAG = CourseActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";

    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        setupActionBar();
        setActionBarElevation(0);

        final Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();

            // TODO Nimmt an, dass alle Intents vom Type "VIEW" für einen Course sind
            if (action != null && action == Intent.ACTION_VIEW) {

                Uri data = intent.getData();
                final String courseIdent = DeepLinkingUtil.getCourseIdentifierFromResumeUri(data);
                System.out.println("CourseIdent: " + courseIdent);

                Result<List<Course>> result = new Result<List<Course>>() {

                    @Override
                    protected void onSuccess(List<Course> result, DataSource dataSource) {
                        super.onSuccess(result, dataSource);

                        for (Course course : result) {
                            if (course.course_code.equals(courseIdent)) {
                                mCourse = course;
                                if(mCourse.locked) {
                                    setTitle(mCourse.name);

                                    String tag = "content";

                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    if (fragmentManager.findFragmentByTag(tag) == null) {
                                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                                        transaction.replace(R.id.content, WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code, false, false), tag);
                                        transaction.commit();
                                    }
                                } else {
                                    handleCourseData();
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    protected void onWarning(WarnCode warnCode) {
                        super.onWarning(warnCode);

                        System.out.println("COURSE RESULT WARNING");
                    }

                    @Override
                    protected void onError(ErrorCode errorCode) {
                        super.onError(errorCode);

                        // TODO Was wenn keine Netzwerkverbindung?
                        System.out.println("COURSE RESULT ERROR");
                        finish();
                    }
                };

                CourseModel courseModel = new CourseModel(this, jobManager, databaseHelper);
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
            transaction.replace(R.id.content, CourseFragment.newInstance(mCourse), tag);
            transaction.commit();
        }
    }

}
