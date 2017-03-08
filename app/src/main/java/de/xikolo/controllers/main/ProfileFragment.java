package de.xikolo.controllers.main;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.controllers.fragments.MainFragment;
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.controllers.navigation.adapter.NavigationAdapter;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Enrollment;
import de.xikolo.models.User;
import de.xikolo.presenters.PresenterFactory;
import de.xikolo.presenters.ProfilePresenter;
import de.xikolo.presenters.ProfilePresenterFactory;
import de.xikolo.presenters.ProfileView;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ToastUtil;
import de.xikolo.views.CustomSizeImageView;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ProfileFragment extends MainFragment<ProfilePresenter, ProfileView> implements ProfileView {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private UserManager userManager;
    private CourseManager courseManager;
    private Result<User> userResult;

    @BindView(R.id.textName) private TextView textName;
    @BindView(R.id.imageHeader) private CustomSizeImageView imageHeader;
    @BindView(R.id.imageProfile) private CustomSizeImageView imageProfile;
    @BindView(R.id.textEnrollCount) private TextView textEnrollCounts;
    @BindView(R.id.textEmail) private TextView textEmail;

    private RealmResults enrollmentListPromise;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        courseManager = new CourseManager();

        userManager = new UserManager();
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

        ButterKnife.bind(this, view);

        updateLayout();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        enrollmentListPromise = courseManager.listEnrollmentsAsync(realm, new RealmChangeListener<RealmResults<Enrollment>>() {
            @Override
            public void onChange(RealmResults<Enrollment> enrollments) {
                Log.w(TAG, "Enrollments: " + enrollments.size());
                showEnrollmentCount(enrollments.size());
            }
        });

        if (UserManager.isLoggedIn()) {
            showHeader();
            userManager.getUser(userResult);
            courseManager.requestCourses();
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (enrollmentListPromise != null) {
            enrollmentListPromise.removeChangeListeners();
        }
    }

    private void updateLayout() {
        notificationController.showProgress(false);
        showHeader();
        showUser(UserManager.getSavedUser());
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

        ImageHelper.load(R.drawable.title, imageHeader);

        imageProfile.setDimensions(heightProfile, heightProfile);
        imageProfile.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        if (user.user_visual != null) {
            ImageHelper.loadRounded(user.user_visual, imageProfile, heightProfile, heightProfile);
        } else {
            ImageHelper.loadRounded(R.drawable.avatar, imageProfile, heightProfile, heightProfile);
        }

        textEmail.setText(user.email);
    }

    private void setProfilePicMargin() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageProfile.getLayoutParams();
        layoutParams.setMargins(0, imageHeader.getMeasuredHeight() - (imageProfile.getMeasuredHeight() / 2), 0, 0);
        imageProfile.setLayoutParams(layoutParams);
    }

    private void showHeader() {
        User user = UserManager.getSavedUser();
        activityCallback.onFragmentAttached(NavigationAdapter.NAV_PROFILE.getPosition(), user.first_name + " " + user.last_name);
    }

    private void showEnrollmentCount(int count) {
        if (textEnrollCounts != null) {
            textEnrollCounts.setText(String.valueOf(count));
        }
    }

    @NonNull
    @Override
    protected PresenterFactory<ProfilePresenter> getPresenterFactory() {
        return new ProfilePresenterFactory();
    }

}
