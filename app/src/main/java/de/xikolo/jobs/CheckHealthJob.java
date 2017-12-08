package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import java.util.Date;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import okhttp3.internal.http.HttpDate;
import retrofit2.Response;

public class CheckHealthJob extends BaseJob {

    public static final String TAG = CheckHealthJob.class.getSimpleName();

    public CheckHealthJob(JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added");
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {

            Response<Void> response = ApiService.getInstance().base().execute();

            if (response.isSuccessful()) {
                String apiVersionExpirationDate = response.headers().get(Config.HEADER_API_VERSION_EXPIRATION_DATE);
                if (apiVersionExpirationDate != null) {
                    if (Config.DEBUG) Log.e(TAG, "Health check: api deprecated and will expire at " + apiVersionExpirationDate);
                    Date expirationDate = HttpDate.parse(apiVersionExpirationDate);
                    if (callback != null) callback.deprecated(expirationDate);
                } else {
                    if (Config.DEBUG) Log.i(TAG, "Health check: successful");
                    if (callback != null) callback.success();
                }
            } else if (response.code() == 406) {
                if (Config.DEBUG) Log.e(TAG, "Health check: api version expired");
                if (callback != null) callback.error(JobCallback.ErrorCode.API_VERSION_EXPIRED);
            } else if (response.code() == 503) {
                if (Config.DEBUG) Log.e(TAG, "Health check: server maintenance ongoing");
                if (callback != null) callback.error(JobCallback.ErrorCode.MAINTENANCE);
            } else {
                if (Config.DEBUG) Log.e(TAG, "Health check: unclassified error");
                if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
            }
        } else {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
