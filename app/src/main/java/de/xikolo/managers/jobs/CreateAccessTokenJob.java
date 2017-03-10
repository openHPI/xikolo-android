package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import org.greenrobot.eventbus.EventBus;

import de.xikolo.events.LoginEvent;
import de.xikolo.models.AccessToken;
import de.xikolo.network.ApiService;
import de.xikolo.storages.preferences.UserStorage;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import retrofit2.Response;

public class CreateAccessTokenJob extends BaseJob {

    public static final String TAG = CreateAccessTokenJob.class.getSimpleName();

    private String email;
    private String password;

    public CreateAccessTokenJob(JobCallback callback, String email, String password) {
        super(new Params(Priority.HIGH), callback);
        this.callback = callback;
        this.email = email;
        this.password = password;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | email " + email);
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {

            final Response<AccessToken> response = ApiService.getInstance().createToken(
                    Config.API + Config.AUTHENTICATE,
                    email,
                    password
            ).execute();

            if (response.isSuccessful()) {
                AccessToken token = response.body();

                if (Config.DEBUG) Log.i(TAG, "AccessToken created");

                UserStorage userStorage = new UserStorage();
                userStorage.saveAccessToken(token.token);
                userStorage.saveUserId(token.userId);

                callback.onSuccess();

                EventBus.getDefault().post(new LoginEvent());
            } else {
                if (Config.DEBUG) Log.w(TAG, "AccessToken not created");
                callback.onError(JobCallback.ErrorCode.ERROR);
            }
        } else {
            callback.onError(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
