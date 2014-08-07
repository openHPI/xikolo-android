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
import de.xikolo.controller.main.adapter.EnrollmentProgressListAdapter;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.manager.CourseManager;
import de.xikolo.manager.EnrollmentManager;
import de.xikolo.manager.TokenManager;
import de.xikolo.manager.UserManager;
import de.xikolo.model.AccessToken;
import de.xikolo.model.Course;
import de.xikolo.model.Enrollment;
import de.xikolo.model.User;
import de.xikolo.util.Network;
import de.xikolo.util.Path;
import de.xikolo.util.Toaster;
import de.xikolo.view.CircularImageView;
import de.xikolo.view.CustomSizeImageView;

public class ProfileFragment extends ContentFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;

    private TokenManager tokenManager;
    private UserManager userManager;
    private EnrollmentManager enrollManager;
    private CourseManager courseManager;

    private ProgressBar mProgress;

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
    private ProgressBar mProgressBar;

    private List<Enrollment> mEnrollments;
    private List<Course> mCourses;

    private static final String KEY_ENROLLMENTS = "key_enrollments";
    private static final String KEY_COURSES = "key_courses";

    private EnrollmentProgressListAdapter mAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mEnrollments != null) {
            outState.putParcelableArrayList(KEY_ENROLLMENTS, (ArrayList<Enrollment>) mEnrollments);
        }
        if (mCourses != null) {
            outState.putParcelableArrayList(KEY_COURSES, (ArrayList<Course>) mCourses);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        if (savedInstanceState != null) {
            mEnrollments = savedInstanceState.getParcelableArrayList(KEY_ENROLLMENTS);
            mCourses = savedInstanceState.getParcelableArrayList(KEY_COURSES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mProgress = (ProgressBar) view.findViewById(R.id.progress);

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
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mAdapter = new EnrollmentProgressListAdapter(getActivity());
        mProgressListView.setAdapter(mAdapter);

        enrollManager = new EnrollmentManager(getActivity()) {
            @Override
            public void onEnrollmentsRequestReceived(List<Enrollment> enrolls) {
                if (enrolls != null) {
                    mEnrollments = enrolls;
                    mAdapter.updateEnrollments(mEnrollments);
                    mTextEnrollCounts.setText(String.valueOf(enrolls.size()));
                    mCallback.updateDrawer();
                    courseManager.requestCourses(false, true);
                }
            }

            @Override
            public void onEnrollmentsRequestCancelled() {
            }
        };

        courseManager = new CourseManager(getActivity()) {
            @Override
            public void onCoursesRequestReceived(List<Course> courses) {
                if (courses != null) {
                    mCourses = courses;
                    mAdapter.updateCourses(courses);
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCoursesRequestCancelled() {
            }
        };

        userManager = new UserManager(getActivity()) {
            @Override
            public void onUserRequestReceived(User user) {
                switchView();
                showUser(user);
                mCallback.updateDrawer();
            }

            @Override
            public void onUserRequestCancelled() {
            }
        };

        tokenManager = new TokenManager(getActivity()) {
            @Override
            public void onAccessTokenRequestReceived(AccessToken token) {
                userManager.requestUser();
                enrollManager.requestEnrollments(false);
            }

            @Override
            public void onAccessTokenRequestCancelled() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.show(getActivity(), R.string.toast_log_in_failed);
                        mContainerLogin.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.GONE);
                    }
                });
            }
        };
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                if (Network.isOnline(getActivity())) {
                    String email = mEditEmail.getText().toString().trim();
                    String password = mEditPassword.getText().toString();
                    if (isEmailValid(email)) {
                        if (password != null && !password.equals("")) {
                            tokenManager.login(email, password);
                            mContainerLogin.setVisibility(View.GONE);
                            mProgress.setVisibility(View.VISIBLE);
                        } else {
                            mEditPassword.setError(getString(R.string.error_password));
                        }
                    } else {
                        mEditEmail.setError(getString(R.string.error_email));
                    }
                } else {
                    Network.showNoConnectionToast(getActivity());
                }
            }
        });
        mBtnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                startUrlIntent(Path.URI_HPI + Path.ACCOUNT + Path.NEW);
            }
        });
        mTextReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                startUrlIntent(Path.URI_HPI + Path.ACCOUNT + Path.RESET);
            }
        });
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                tokenManager.logout();
                mEnrollments = null;
                mCourses = null;
                switchView();
                mCallback.updateDrawer();
            }
        });

        switchView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        switchHeader();
        if (TokenManager.isLoggedIn(getActivity()) && Network.isOnline(getActivity())) {
            userManager.requestUser();
            if (mEnrollments != null && mCourses != null) {
                mAdapter.updateEnrollments(mEnrollments);
                mTextEnrollCounts.setText(String.valueOf(mEnrollments.size()));
                mCallback.updateDrawer();
                mAdapter.updateCourses(mCourses);
                mProgressBar.setVisibility(View.GONE);
            } else {
                enrollManager.requestEnrollments(true);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void switchView() {
        mProgress.setVisibility(View.GONE);
        mEditEmail.setText(null);
        mEditPassword.setText(null);
        if (!TokenManager.isLoggedIn(getActivity())) {
            mContainerLogin.setVisibility(View.VISIBLE);
            mContainerProfile.setVisibility(View.GONE);
        } else {
            mContainerLogin.setVisibility(View.GONE);
            mContainerProfile.setVisibility(View.VISIBLE);
            showUser(UserManager.getUser(getActivity()));
            setProfilePicMargin();
        }
        switchHeader();
    }

    private void showUser(User user) {
        mTextName.setText(user.name);
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

        mTextEnrollCounts.setText(String.valueOf(EnrollmentManager.getEnrollmentsSize(getActivity())));
    }

    private void setProfilePicMargin() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImgProfile.getLayoutParams();
        layoutParams.setMargins(0, mImgHeader.getMeasuredHeight() - (mImgProfile.getMeasuredHeight() / 2), 0, 0);
        mImgProfile.setLayoutParams(layoutParams);
    }

    private void switchHeader() {
        if (!TokenManager.isLoggedIn(getActivity())) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_PROFILE, getString(R.string.title_section_login));
        } else {
            User user = UserManager.getUser(getActivity());
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_PROFILE, user.name);
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
