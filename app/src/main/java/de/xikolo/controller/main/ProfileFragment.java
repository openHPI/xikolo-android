package de.xikolo.controller.main;

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.User;
import de.xikolo.model.CourseModel;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;
import de.xikolo.view.CircularImageView;
import de.xikolo.view.CustomSizeImageView;

public class ProfileFragment extends ContentFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private static final String KEY_COURSES = "key_courses";

    private UserModel mUserModel;
    private CourseModel mCourseModel;
    private Result<List<Course>> mCoursesResult;
    private Result<User> mUserResult;

    private NotificationController mNotificationController;

    private TextView mTextName;
    private CustomSizeImageView mImgHeader;
    private CircularImageView mImgProfile;
    private TextView mTextEnrollCounts;
    private TextView mTextEmail;

    private List<Course> mCourses;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCourses != null) {
            outState.putParcelableArrayList(KEY_COURSES, (ArrayList<Course>) mCourses);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCourses = savedInstanceState.getParcelableArrayList(KEY_COURSES);
        }

        mCourseModel = new CourseModel(jobManager);
        mCoursesResult = new Result<List<Course>>() {
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

        mUserModel = new UserModel(jobManager);
        mUserResult = new Result<User>() {
            @Override
            protected void onSuccess(User result, DataSource dataSource) {
                updateLayout();
                mActivityCallback.updateDrawer();
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

        mNotificationController = new NotificationController(view);

        mTextName = (TextView) view.findViewById(R.id.textName);
        mImgHeader = (CustomSizeImageView) view.findViewById(R.id.imageHeader);
        mImgProfile = (CircularImageView) view.findViewById(R.id.imageProfile);
        mTextEnrollCounts = (TextView) view.findViewById(R.id.textEnrollCount);
        mTextEmail = (TextView) view.findViewById(R.id.textEmail);

        updateLayout();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (UserModel.isLoggedIn(getActivity())) {
            showHeader();
            if (mCourses == null) {
                mUserModel.getUser(mUserResult);
                mCourseModel.getCourses(mCoursesResult, false);
            } else {
                showCoursesProgress(mCourses);
            }
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void updateLayout() {
        mNotificationController.setProgressVisible(false);
        showHeader();
        showUser(UserModel.getSavedUser(getActivity()));
        setProfilePicMargin();
    }

    private void showUser(User user) {
        mTextName.setText(String.format(getString(R.string.user_name), user.first_name, user.last_name));
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
        mImgHeader.setDimensions(size.x, heightHeader);
        mImgHeader.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        ImageLoader.getInstance().displayImage("drawable://" + R.drawable.title, mImgHeader);
        mImgProfile.setDimensions(heightProfile, heightProfile);
        mImgProfile.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        if (user.user_visual != null) {
            Drawable lastImage;
            if (mImgProfile.getDrawable() != null) {
                lastImage = mImgProfile.getDrawable();
            } else {
                lastImage = ContextCompat.getDrawable(getActivity(), R.drawable.avatar);
            }
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(lastImage)
                    .showImageForEmptyUri(R.drawable.avatar)
                    .showImageOnFail(R.drawable.avatar)
                    .build();
            ImageLoader.getInstance().displayImage(user.user_visual, mImgProfile, options);
        } else {
            ImageLoader.getInstance().displayImage("drawable://" + R.drawable.avatar, mImgProfile);
        }

        mTextEmail.setText(user.email);

        mTextEnrollCounts.setText(String.valueOf(mCourseModel.getEnrollmentsCount()));
    }

    private void setProfilePicMargin() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImgProfile.getLayoutParams();
        layoutParams.setMargins(0, mImgHeader.getMeasuredHeight() - (mImgProfile.getMeasuredHeight() / 2), 0, 0);
        mImgProfile.setLayoutParams(layoutParams);
    }

    private void showHeader() {
        User user = UserModel.getSavedUser(getActivity());
        mActivityCallback.onFragmentAttached(NavigationAdapter.NAV_ID_PROFILE, user.first_name + " " + user.last_name);
    }

    private void showCoursesProgress(List<Course> courses) {
        mCourses = courses;
        mTextEnrollCounts.setText(String.valueOf(mCourseModel.getEnrollmentsCount()));
        if (mActivityCallback != null) {
            mActivityCallback.updateDrawer();
        }
    }

}
