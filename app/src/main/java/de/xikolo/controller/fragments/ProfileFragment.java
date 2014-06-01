package de.xikolo.controller.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.xikolo.R;
import de.xikolo.controller.navigation.adapter.NavigationAdapter;
import de.xikolo.manager.AccessTokenManager;
import de.xikolo.manager.UserManager;
import de.xikolo.model.AccessToken;
import de.xikolo.model.User;

public class ProfileFragment extends ContentFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;

    private AccessTokenManager tokenManager;
    private UserManager userManager;

    private ViewGroup mContainerLogin;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnLogin;

    private ViewGroup mContainerProfile;
    private TextView mTextName;
    private Button mBtnLogout;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mContainerLogin = (ViewGroup) view.findViewById(R.id.containerLogin);
        mEditEmail = (EditText) view.findViewById(R.id.editEmail);
        mEditPassword = (EditText) view.findViewById(R.id.editPassword);
        mBtnLogin = (Button) view.findViewById(R.id.btnLogin);

        mContainerProfile = (ViewGroup) view.findViewById(R.id.containerProfile);
        mTextName = (TextView) view.findViewById(R.id.textName);
        mBtnLogout = (Button) view.findViewById(R.id.btnLogout);

        userManager = new UserManager(getActivity()) {
            @Override
            public void onUserRequestReceived(User user) {
                switchView();
                mTextName.setText(user.name);
                mCallback.updateDrawer();
            }

            @Override
            public void onUserRequestCancelled() {
            }
        };

        tokenManager = new AccessTokenManager(getActivity()) {
            @Override
            public void onAccessTokenRequestReceived(AccessToken token) {
                userManager.requestUser();
            }

            @Override
            public void onAccessTokenRequestCancelled() {
                Toast.makeText(getActivity(), "Log in failed", Toast.LENGTH_SHORT).show();
            }
        };
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tokenManager.login(mEditEmail.getText().toString().trim(),
                        mEditPassword.getText().toString());
            }
        });
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tokenManager.logout();
                switchView();
                mCallback.updateDrawer();
            }
        });

        switchView();

        return view;
    }

    private void switchView() {
        if (!AccessTokenManager.isLoggedIn(getActivity())) {
            mContainerLogin.setVisibility(View.VISIBLE);
            mContainerProfile.setVisibility(View.GONE);
        } else {
            mContainerLogin.setVisibility(View.GONE);
            mContainerProfile.setVisibility(View.VISIBLE);
            mTextName.setText(UserManager.getUser(getActivity()).name);
        }
        switchHeader();
    }

    private void switchHeader() {
        if (!AccessTokenManager.isLoggedIn(getActivity())) {
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_PROFILE, getString(R.string.title_section_login));
        } else {
            User user = UserManager.getUser(getActivity());
            mCallback.onTopLevelFragmentAttached(NavigationAdapter.NAV_ID_PROFILE, user.name);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        switchHeader();
    }

}
