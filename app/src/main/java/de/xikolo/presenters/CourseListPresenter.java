package de.xikolo.presenters;

import de.xikolo.R;
import de.xikolo.controllers.navigation.adapter.NavigationAdapter;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.jobs.JobCallback;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;
import io.realm.Realm;

public class CourseListPresenter implements LoadingStatePresenter<CourseListView> {

    CourseListView view;

    CourseManager courseManager;

    Realm realm;

    public CourseListPresenter() {
        this.courseManager = new CourseManager();
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onViewAttached(CourseListView view) {
        this.view = view;
    }

    @Override
    public void onViewDetached() {
        this.view = null;
    }

    @Override
    public void onDestroyed() {
        this.realm.close();
    }

    public void onStart() {
        requestCourses();
    }

    @Override
    public void onRefresh() {
        requestCourses();
    }

    public void onEnrollButtonClicked(String courseId) {
        view.showProgressDialog();

        courseManager.createEnrollment(courseId, new JobCallback() {
            @Override
            public void onSuccess() {
                view.hideProgressDialog();
            }

            @Override
            public void onError(ErrorCode code) {
                view.hideProgressDialog();
                if (code == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                } else if (code == ErrorCode.NO_AUTH) {
                    ToastUtil.show(R.string.toast_please_log_in);
                    activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
                }
            }
        });
    }

    public void onCourseEnterButtonClicked(String courseId) {

    }

    public void onCourseDetailButtonClicked(String courseId) {

    }



    private void requestCourses() {
//        if (isMyCoursesFilter() && !UserManager.isLoggedIn()) {
//            notificationController.setTitle(R.string.notification_please_login);
//            notificationController.setSummary(R.string.notification_please_login_summary);
//            notificationController.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
//                }
//            });
//            notificationController.setNotificationVisible(true);
//            refreshLayout.setRefreshing(false);
//        } else {
//            refreshLayout.setRefreshing(true);
        courseManager.requestCourses();
//        }
    }


}
