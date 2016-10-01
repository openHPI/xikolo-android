package de.xikolo.model.jobs;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.entities.AccessToken;
import de.xikolo.data.net.JsonRequest;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.model.Result;
import de.xikolo.model.events.LoginEvent;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class CreateAccessTokenJob extends Job {

    public static final String TAG = CreateAccessTokenJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private String email;
    private String password;

    private Result<Void> result;

    public CreateAccessTokenJob(Result<Void> result, String email, String password) {
        super(new Params(Priority.HIGH));
        id = jobCounter.incrementAndGet();

        this.result = result;

        this.email = email;
        this.password = password;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | email " + email);
    }

    @Override
    public void onRun() throws Throwable {
        if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_NETWORK);
        } else {
            Type type = new TypeToken<AccessToken>(){}.getType();

            String url = Config.API + Config.AUTHENTICATE + "?email=" + email + "&password=" + URLEncoder.encode(password, "UTF-8");

            JsonRequest request = new JsonRequest(url, type);
            request.setMethod(Config.HTTP_POST);
            request.setCache(false);

            Object o = request.getResponse();
            if (o != null) {
                AccessToken token = (AccessToken) o;
                if (Config.DEBUG) Log.i(TAG, "AccessToken created");

                UserPreferences userPreferences = GlobalApplication.getInstance()
                        .getPreferencesFactory().getUserPreferences();
                userPreferences.saveAccessToken(token);
                result.success(null, Result.DataSource.NETWORK);

                EventBus.getDefault().post(new LoginEvent());
            } else {
                if (Config.DEBUG) Log.w(TAG, "AccessToken not created");
                result.error(Result.ErrorCode.NO_RESULT);
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
