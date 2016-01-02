package de.xikolo.controller;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.xikolo.R;
import de.xikolo.controller.dialogs.UnenrollDialog;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.helper.EnrollmentController;
import de.xikolo.data.entities.Course;
import de.xikolo.util.Config;

public class CourseDetailsActivity extends BaseActivity implements UnenrollDialog.UnenrollDialogListener {

    public static final String TAG = CourseDetailsActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";

    private Course mCourse;

    //private VideoCastManager mCastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coursedetails);
        setupActionBar();

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
            transaction.replace(R.id.content, WebViewFragment.newInstance(Config.URI + Config.COURSES + mCourse.course_code, false, false), tag);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCourse != null && mCourse.is_enrolled) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.coursedetails, menu);
            //mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
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

    @Override
    protected void onPause() {
        //mCastManager.decrementUiCounter();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mCastManager = VideoCastManager.getInstance();
        //mCastManager.incrementUiCounter();
    }

}
