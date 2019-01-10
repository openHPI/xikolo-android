package de.xikolo.controllers.main;

import android.content.res.Configuration;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Profile;
import de.xikolo.models.User;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.main.ProfilePresenter;
import de.xikolo.presenters.main.ProfilePresenterFactory;
import de.xikolo.presenters.main.ProfileView;
import de.xikolo.views.CustomSizeImageView;

public class ProfileFragment extends PresenterMainFragment<ProfilePresenter, ProfileView> implements ProfileView {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    @BindView(R.id.textName) TextView textName;
    @BindView(R.id.imageHeader) CustomSizeImageView imageHeader;
    @BindView(R.id.imageProfile) CustomSizeImageView imageProfile;
    @BindView(R.id.textEnrollCount) TextView textEnrollCounts;
    @BindView(R.id.textEmail) TextView textEmail;

    @Override
    public int getLayoutResource() {
        return R.layout.content_profile;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!UserManager.isAuthorized()) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void showUser(User user, Profile profile) {
        showHeader(profile);

        textName.setText(String.format(getString(R.string.user_name), profile.firstName, profile.lastName));
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

        GlideApp.with(this).load(R.drawable.title).into(imageHeader);

        imageProfile.setDimensions(heightProfile, heightProfile);
        imageProfile.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        GlideApp.with(App.getInstance())
                .load(user.avatarUrl)
                .circleCrop()
                .allPlaceholders(R.drawable.avatar)
                .override(heightProfile)
                .into(imageProfile);

        textEmail.setText(profile.email);

        setProfilePicMargin();
    }

    private void setProfilePicMargin() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageProfile.getLayoutParams();
        layoutParams.setMargins(0, imageHeader.getMeasuredHeight() - (imageProfile.getMeasuredHeight() / 2), 0, 0);
        imageProfile.setLayoutParams(layoutParams);
    }

    private void showHeader(Profile profile) {
        getActivityCallback().onFragmentAttached(NavigationAdapter.NAV_PROFILE.getPosition(), profile.firstName + " " + profile.lastName);
    }

    @Override
    public void showEnrollmentCount(int count) {
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
