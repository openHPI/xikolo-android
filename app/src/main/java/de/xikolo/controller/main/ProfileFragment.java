package de.xikolo.controller.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.main.adapter.CourseProgressListAdapter;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.entities.AccessToken;
import de.xikolo.entities.Course;
import de.xikolo.entities.User;
import de.xikolo.model.CourseModel;
import de.xikolo.model.OnModelResponseListener;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;
import de.xikolo.view.CircularImageView;
import de.xikolo.view.CustomSizeImageView;

public class ProfileFragment extends ContentFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private static final String KEY_COURSES = "key_courses";

    private UserModel mUserModel;
    private CourseModel mCourseModel;

    private ProgressBar mFragmentProgress;
    private ViewGroup mContainerLogin;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnLogin;
    private Button mBtnNew;
    private TextView mTextReset;

    private ViewGroup mContainerProfile;
    private TextView mTextName;
    private Button mBtnLogout;
    private CustomSizeImageView mImgHeader;
    private CircularImageView mImgProfile;
    private TextView mTextEnrollCounts;
    private TextView mTextEmail;
    private ListView mProgressListView;
    private ProgressBar mCoursesProgress;

    private List<Course> mCourses;

    private CourseProgressListAdapter mAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
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

        mCourseModel = new CourseModel(getActivity(), jobManager);
        mCourseModel.setRetrieveCoursesListener(new OnModelResponseListener<List<Course>>() {
            @Override
            public void onResponse(final List<Course> response) {
                if (response != null) {
                    mCourses = response;
                    mAdapter.updateCourses(response);
                    mTextEnrollCounts.setText(String.valueOf(CourseModel.readEnrollmentsSize(getActivity())));
                    mCoursesProgress.setVisibility(View.GONE);
                    mCallback.updateDrawer();
                }
            }
        });

        mUserModel = new UserModel(getActivity(), jobManager);
        mUserModel.setRetrieveUserListener(new OnModelResponseListener<User>() {
            @Override
            public void onResponse(final User response) {
                if (response != null) {
                    showLayout();
                    mCallback.updateDrawer();
                }
            }
        });
        mUserModel.setLoginListener(new OnModelResponseListener<AccessToken>() {
            @Override
            public void onResponse(final AccessToken response) {
                if (response != null) {
                    mUserModel.retrieveUser(false);
                    mCourseModel.retrieveCourses(CourseModel.FILTER_MY, false, true);
                } else {
                    ToastUtil.show(getActivity(), R.string.toast_log_in_failed);
                    mContainerLogin.setVisibility(View.VISIBLE);
                    mFragmentProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mFragmentProgress = (ProgressBar) view.findViewById(R.id.progress);

        mContainerLogin = (ViewGroup) view.findViewById(R.id.containerLogin);
        mEditEmail = (EditText) view.findViewById(R.id.editEmail);
        mEditPassword = (EditText) view.findViewById(R.id.editPassword);
        mBtnLogin = (Button) view.findViewById(R.id.btnLogin);
        mBtnNew = (Button) view.findViewById(R.id.btnNew);
        mTextReset = (TextView) view.findViewById(R.id.textForgotPw);

        mContainerProfile = (ViewGroup) view.findViewById(R.id.containerProfile);
        mTextName = (TextView) view.findViewById(R.id.textName);
        mBtnLogout = (Button) view.findViewById(R.id.btnLogout);
        mImgHeader = (CustomSizeImageView) view.findViewById(R.id.imageHeader);
        mImgProfile = (CircularImageView) view.findViewById(R.id.imageProfile);
        mTextEnrollCounts = (TextView) view.findViewById(R.id.textEnrollCount);
        mTextEmail = (TextView) view.findViewById(R.id.textEmail);
        mProgressListView = (ListView) view.findViewById(R.id.listView);
        mCoursesProgress = (ProgressBar) view.findViewById(R.id.progressBar);

        mAdapter = new CourseProgressListAdapter(getActivity());
        mProgressListView.setAdapter(mAdapter);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                if (NetworkUtil.isOnline(getActivity())) {
                    String email = mEditEmail.getText().toString().trim();
                    String password = mEditPassword.getText().toString();
                    if (isEmailValid(email)) {
                        if (password != null && !password.equals("")) {
                            mUserModel.login(email, password);
                            mContainerLogin.setVisibility(View.GONE);
                            mFragmentProgress.setVisibility(View.VISIBLE);
                        } else {
                            mEditPassword.setError(getString(R.string.error_password));
                        }
                    } else {
                        mEditEmail.setError(getString(R.string.error_email));
                    }
                } else {
                    NetworkUtil.showNoConnectionToast(getActivity());
                }
            }
        });
        mBtnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                startUrlIntent(Config.URI + Config.ACCOUNT + Config.NEW);
            }
        });
        mTextReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                startUrlIntent(Config.URI + Config.ACCOUNT + Config.RESET);
            }
        });
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                UserModel.logout(getActivity());
                mCourses = null;
                showLayout();
                mCallback.updateDrawer();
            }
        });

        showLayout();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showHeader();
        if (UserModel.isLoggedIn(getActivity()) && NetworkUtil.isOnline(getActivity())) {
            mUserModel.retrieveUser(true);
            mCourseModel.retrieveCourses(CourseModel.FILTER_MY, true, true);
            mCoursesProgress.setVisibility(View.VISIBLE);
        } else if (UserModel.isLoggedIn(getActivity()) && !NetworkUtil.isOnline(getActivity())) {
            NetworkUtil.showNoConnectionToast(getActivity());
            mCoursesProgress.setVisibility(View.GONE);
        }
    }

    private void showLayout() {
        mFragmentProgress.setVisibility(View.GONE);
        mEditEmail.setText(null);
        mEditPassword.setText(null);
        if (!UserModel.isLoggedIn(getActivity())) {
            mContainerLogin.setVisibility(View.VISIBLE);
            mContainerProfile.setVisibility(View.GONE);
        } else {
            mContainerLogin.setVisibility(View.GONE);
            mContainerProfile.setVisibility(View.VISIBLE);
            showUser(UserModel.readUser(getActivity()));
            setProfilePicMargin();
        }
        showHeader();
    }

    private void showUser(User user) {
        mTextName.setText(user.first_name + " " + user.last_name);
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
        mImgProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.avatar));

        mTextEmail.setText(user.email);

        mTextEnrollCounts.setText(String.valueOf(CourseModel.readEnrollmentsSize(getActivity())));
    }

    private void setProfilePicMargin() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImgProfile.getLayoutParams();
        layoutParams.setMargins(0, mImgHeader.getMeasuredHeight() - (mImgProfile.getMeasuredHeight() / 2), 0, 0);
        mImgProfile.setLayoutParams(layoutParams);
    }

    private void showHeader() {
        if (!UserModel.isLoggedIn(getActivity())) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_PROFILE, getString(R.string.title_section_login));
        } else {
            User user = UserModel.readUser(getActivity());
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_PROFILE, user.first_name + " " + user.last_name);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void startUrlIntent(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
