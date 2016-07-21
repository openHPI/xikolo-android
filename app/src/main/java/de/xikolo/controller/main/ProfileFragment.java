package de.xikolo.controller.main;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.helper.ImageController;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.User;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;
import de.xikolo.view.CustomSizeImageView;

public class ProfileFragment extends ContentFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private static final String ARG_COURSES = "arg_courses";

    private UserModel userModel;
    private CourseModel courseModel;
    private Result<List<Course>> coursesResult;
    private Result<User> userResult;

    private NotificationController notificationController;

    private TextView textName;
    private CustomSizeImageView imageHeader;
    private CustomSizeImageView imageProfile;
    private TextView textEnrollCounts;
    private TextView textEmail;

    private List<Course> courses;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (courses != null) {
            outState.putParcelableArrayList(ARG_COURSES, (ArrayList<Course>) courses);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            courses = savedInstanceState.getParcelableArrayList(ARG_COURSES);
        }

        courseModel = new CourseModel(jobManager);
        coursesResult = new Result<List<Course>>() {
            @Override
            protected void onSuccess(List<Course> result, DataSource dataSource) {
                showCoursesProgress(result);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                }
            }
        };

        userModel = new UserModel(jobManager);
        userResult = new Result<User>() {
            @Override
            protected void onSuccess(User result, DataSource dataSource) {
                updateLayout();
                activityCallback.updateDrawer();
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                if (errorCode == ErrorCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast();
                } else {
                    ToastUtil.show(R.string.toast_log_in_failed);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        notificationController = new NotificationController(view);

        textName = (TextView) view.findViewById(R.id.textName);
        imageHeader = (CustomSizeImageView) view.findViewById(R.id.imageHeader);
        imageProfile = (CustomSizeImageView) view.findViewById(R.id.imageProfile);
        textEnrollCounts = (TextView) view.findViewById(R.id.textEnrollCount);
        textEmail = (TextView) view.findViewById(R.id.textEmail);

        updateLayout();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (UserModel.isLoggedIn(getActivity())) {
            showHeader();
            if (courses == null) {
                userModel.getUser(userResult);
                courseModel.getCourses(coursesResult, false);
            } else {
                showCoursesProgress(courses);
            }
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void updateLayout() {
        notificationController.setProgressVisible(false);
        showHeader();
        showUser(UserModel.getSavedUser(getActivity()));
        setProfilePicMargin();
    }

    private void showUser(User user) {
        textName.setText(String.format(getString(R.string.user_name), user.first_name, user.last_name));
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int heightHeader;
        int heightProfile;
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            heightHeader = (int) (size.y * 0.2);
            heightProfile = (int) (size.x * 0.2);
        } else {
            heightHeader = (int) (size.y * 0.35);
            heightProfile = (int) (size.y * 0.2);
        }
        imageHeader.setDimensions(size.x, heightHeader);
        imageHeader.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        ImageController.load(R.drawable.title, imageHeader);

        imageProfile.setDimensions(heightProfile, heightProfile);
        imageProfile.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        if (user.user_visual != null) {
            ImageController.loadRounded(user.user_visual, imageProfile, heightProfile, heightProfile);
        } else {
            ImageController.loadRounded(R.drawable.avatar, imageProfile, heightProfile, heightProfile);
        }

        textEmail.setText(user.email);

        textEnrollCounts.setText(String.valueOf(courseModel.getEnrollmentsCount()));
    }

    private void setProfilePicMargin() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageProfile.getLayoutParams();
        layoutParams.setMargins(0, imageHeader.getMeasuredHeight() - (imageProfile.getMeasuredHeight() / 2), 0, 0);
        imageProfile.setLayoutParams(layoutParams);
    }

    private void showHeader() {
        User user = UserModel.getSavedUser(getActivity());
        activityCallback.onFragmentAttached(NavigationAdapter.NAV_PROFILE.getPosition(), user.first_name + " " + user.last_name);
    }

    private void showCoursesProgress(List<Course> courses) {
        this.courses = courses;
        textEnrollCounts.setText(String.valueOf(courseModel.getEnrollmentsCount()));
        if (activityCallback != null) {
            activityCallback.updateDrawer();
        }
    }

}
