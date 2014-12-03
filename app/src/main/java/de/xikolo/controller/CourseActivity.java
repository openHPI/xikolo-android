package de.xikolo.controller;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import de.xikolo.R;
import de.xikolo.controller.course.CourseFragment;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.data.entities.Course;

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

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(ARG_COURSE)) {
            throw new WrongParameterException();
        } else {
            this.mCourse = b.getParcelable(ARG_COURSE);
        }

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
