package de.xikolo.controller.helper;

import android.support.v4.app.FragmentActivity;

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.dialogs.ProgressDialog;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.model.events.UnenrollEvent;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class EnrollmentController {

    public static void unenroll(final FragmentActivity activity, Course course) {
        CourseModel model = new CourseModel(GlobalApplication.getInstance().getJobManager());
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
                    NetworkUtil.showNoConnectionToast(activity);
                } else {
                    ToastUtil.show(activity, R.string.error);
                }
            }
        };
        progressDialog.show(activity.getSupportFragmentManager(), ProgressDialog.TAG);
        model.deleteEnrollment(result, course);
    }

}
