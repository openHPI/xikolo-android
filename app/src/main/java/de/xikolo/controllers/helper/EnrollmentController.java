package de.xikolo.controllers.helper;

import android.support.v4.app.FragmentActivity;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.managers.Result;
import de.xikolo.events.UnenrollEvent;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;

public class EnrollmentController {

    public static void unenroll(final FragmentActivity activity, Course course) {
        CourseManager courseManager = new CourseManager(GlobalApplication.getInstance().getJobManager());
        final ProgressDialog progressDialog = ProgressDialog.getInstance();
        Result<Course> result = new Result<Course>() {
            @Override
            protected void onSuccess(Course result, DataSource dataSource) {
                progressDialog.dismiss();
                EventBus.getDefault().post(new UnenrollEvent(result));
                activity.finish();
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                progressDialog.dismiss();
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                } else {
                    ToastUtil.show(R.string.error);
                }
            }
        };
        progressDialog.show(activity.getSupportFragmentManager(), ProgressDialog.TAG);
        courseManager.deleteEnrollment(result, course);
    }

}
