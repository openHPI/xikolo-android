package de.xikolo.managers.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.UserManager;
import de.xikolo.models.User;
import de.xikolo.network.ApiRequest;
import de.xikolo.network.parser.ApiParser;
import de.xikolo.storages.preferences.StorageType;
import de.xikolo.storages.preferences.UserStorage;
import de.xikolo.managers.Result;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import okhttp3.Response;

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
        if (!UserManager.isLoggedIn()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            UserStorage userStorage = (UserStorage) GlobalApplication.getStorage(StorageType.USER);

            result.success(userStorage.getUser(), Result.DataSource.LOCAL);

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.USER;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    User user = ApiParser.parse(response, User.class);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "User received: " + user.first_name);

                    userStorage.saveUser(user);
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
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
