package de.xikolo.openhpi.controller.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.manager.AccessTokenManager;
import de.xikolo.openhpi.manager.UserManager;
import de.xikolo.openhpi.model.AccessToken;
import de.xikolo.openhpi.model.User;

public class SettingsFragment extends ContentFragment {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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

        final UserManager userMgmt = new UserManager(getActivity()) {
            @Override
            public void onUserRequestReceived(User user) {
                Log.e(TAG, user.name);
                Log.e(TAG, AccessTokenManager.getAccessToken(getActivity()).access_token);
            }

            @Override
            public void onUserRequestCancelled() {

            }
        };

        final AccessTokenManager tokenMgmt = new AccessTokenManager(getActivity()) {
            @Override
            public void onAccessTokenRequestReceived(AccessToken token) {
                userMgmt.requestUser();
            }

            @Override
            public void onAccessTokenRequestCancelled() {

            }
        };
//        tokenMgmt.login("tobias.rohloff@student.hpi.uni-potsdam.de", "openhpi");

    }

    @Override
    public void onStart() {
        super.onStart();
        mCallback.onTopFragmentAttached(3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

}
