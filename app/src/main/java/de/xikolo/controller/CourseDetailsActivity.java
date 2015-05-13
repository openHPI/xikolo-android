package de.xikolo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.dialogs.UnenrollDialog;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.helper.EnrollmentController;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.util.Config;
import de.xikolo.util.DeepLinkingUtil;

public class CourseDetailsActivity extends BaseActivity implements UnenrollDialog.UnenrollDialogListener {

    public static final String TAG = CourseDetailsActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";

    private Course mCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coursedetails);
        setupActionBar();

        final Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();

            // TODO Nimmt an, dass alle Intents vom Type "VIEW" für einen Course sind
            if (action != null && action == Intent.ACTION_VIEW) {

                Result<List<Course>> result = new Result<List<Course>>() {

                    @Override
                    protected void onSuccess(List<Course> result, DataSource dataSource) {
                        super.onSuccess(result, dataSource);

                        Uri data = intent.getData();
                        String courseIdent = DeepLinkingUtil.getCourseIdentifierFromUri(data);
                        System.out.println("COURSE_IDENT: " + courseIdent);

                        for (Course course : result) {
                            if (course.course_code.equals(courseIdent)) {
                                System.out.println("FOUND COURSE");
                                mCourse = course;
                                handleCourseData();
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

                        System.out.println("COURSE RESULT ERROR");
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
            transaction.replace(R.id.content, WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code, false, false), tag);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCourse != null && mCourse.is_enrolled) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.course, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
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
        EnrollmentController.unenroll(this, mCourse);
    }
}
