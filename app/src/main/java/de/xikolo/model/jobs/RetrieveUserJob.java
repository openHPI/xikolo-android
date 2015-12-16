package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.entities.User;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class RetrieveUserJob extends Job {

    public static final String TAG = RetrieveUserJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<User> result;

    public RetrieveUserJob(Result<User> result) {
        super(new Params(Priority.HIGH));
        id = jobCounter.incrementAndGet();

        this.result = result;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            UserPreferences userPreferences = GlobalApplication.getInstance()
                    .getPreferencesFactory().getUserPreferences();

            result.success(userPreferences.getUser(), Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                Type type = new TypeToken<User>(){}.getType();

                String url = Config.API + Config.USER;

                JsonRequest request = new JsonRequest(url, type);
                request.setCache(false);

                request.setToken(UserModel.getToken(GlobalApplication.getInstance()));

                Object o = request.getResponse();
                if (o != null) {
                    User user = (User) o;
                    if (Config.DEBUG) Log.i(TAG, "User received: " + user.first_name);
                    userPreferences.saveUser(user);
                    result.success(user, Result.DataSource.NETWORK);
                } else {
                    if (Config.DEBUG) Log.w(TAG, "No User received");
                    result.error(Result.ErrorCode.NO_RESULT);
                }
            } else {
                result.warn(Result.WarnCode.NO_NETWORK);
            }
        }
    }

    @Override
    protected void onCancel() {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}
