package de.xikolo.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.path.android.jobqueue.JobManager;

import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.entities.AccessToken;
import de.xikolo.entities.User;
import de.xikolo.jobs.CreateAccessTokenJob;
import de.xikolo.jobs.CreateSessionJob;
import de.xikolo.jobs.OnJobResponseListener;
import de.xikolo.jobs.RetrieveUserJob;

public class UserModel extends BaseModel {

    public static final String TAG = UserModel.class.getSimpleName();

    private static boolean hasSession = false;

    private UserPreferences mUserPref;

    private OnModelResponseListener<User> mUserListener;
    private OnModelResponseListener<AccessToken> mTokenListener;
    private OnModelResponseListener<Void> mSessionListener;

    public UserModel(Context context, JobManager jobManager) {
        super(context, jobManager);

        this.mUserPref = new UserPreferences(context);
    }

    public static boolean hasSession() {
        return hasSession;
    }

    public static User readUser(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getUser();
    }

    public static String readAccessToken(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return prefs.getAccessToken().access_token;
    }

    public static boolean isLoggedIn(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        return !prefs.getAccessToken().access_token
                .equals(UserPreferences.ACCESS_TOKEN_DEFAULT);
    }

    public static void logout(Context context) {
        UserPreferences prefs = new UserPreferences(context);
        prefs.deleteUser();
        EnrollmentModel.deleteEnrollmentsSize(context);
    }

    public void login(String email, String password) {
        OnJobResponseListener<AccessToken> callback = new OnJobResponseListener<AccessToken>() {
            @Override
            public void onResponse(final AccessToken response) {
                if (mTokenListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mTokenListener.onResponse(response);
                        }
                    });
                }
                if (response != null)
                    mUserPref.saveAccessToken(response);
            }

            @Override
            public void onCancel() {
                if (mTokenListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mTokenListener.onResponse(null);
                        }
                    });
                }
            }
        };
        mJobManager.addJobInBackground(new CreateAccessTokenJob(callback, email, password));
    }

    public void setLoginListener(OnModelResponseListener<AccessToken> listener) {
        mTokenListener = listener;
    }

    public void retrieveUser(boolean cache) {
        OnJobResponseListener<User> callback = new OnJobResponseListener<User>() {
            @Override
            public void onResponse(final User response) {
                if (mUserListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mUserListener.onResponse(response);
                        }
                    });
                }
                if (response != null)
                    mUserPref.saveUser(response);
            }

            @Override
            public void onCancel() {
                if (mUserListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mUserListener.onResponse(null);
                        }
                    });
                }
            }
        };
        mJobManager.addJobInBackground(new RetrieveUserJob(callback, cache, readAccessToken(mContext)));
    }

    public void setRetrieveUserListener(OnModelResponseListener<User> listener) {
        mUserListener = listener;
    }

    public void createSession() {
        OnJobResponseListener<Void> callback = new OnJobResponseListener<Void>() {
            @Override
            public void onResponse(final Void response) {
                if (mSessionListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mSessionListener.onResponse(response);
                        }
                    });
                }
                hasSession = true;
            }

            @Override
            public void onCancel() {
                if (mUserListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mSessionListener.onResponse(null);
                        }
                    });
                }
            }
        };
        mJobManager.addJobInBackground(new CreateSessionJob(callback, readAccessToken(mContext)));
    }

    public void setCreateSessionListener(OnModelResponseListener<Void> listener) {
        mSessionListener = listener;
    }

}
