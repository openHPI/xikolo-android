package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.entities.AccessToken;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.data.preferences.UserPreferences;
import de.xikolo.model.Result;
import de.xikolo.model.events.LoginEvent;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

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
            String url = Config.API + Config.AUTHENTICATE;

            RequestBody body = new FormBody.Builder()
                    .add("email", email)
                    .add("password", password)
                    .build();

            Response response = new ApiRequest(url)
                    .post(body)
                    .execute();

            if (response.isSuccessful()) {
                AccessToken token = ApiParser.parse(response, AccessToken.class);
                response.close();

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
