package de.xikolo.managers.jobs;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.GlobalApplication;
import de.xikolo.managers.Result;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.network.ApiRequest;
import de.xikolo.storages.databases.DataType;
import de.xikolo.storages.databases.adapters.ItemDataAdapter;
import de.xikolo.utils.Config;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateProgressionJob extends Job {

    public static final String TAG = UpdateProgressionJob.class.getSimpleName();

    private Item item;
    private transient Result<Void> result;

    public UpdateProgressionJob(Result<Void> result, Item item) {
        super(new Params(Priority.LOW).requireNetwork().persist());

        this.result = result;
        this.item = item;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | item.id " + item.id);
        Log.d(TAG, "update progression added");

        ItemDataAdapter itemDataAccess = (ItemDataAdapter) GlobalApplication.getDataAdapter(DataType.ITEM);

        item.progress.visited = true;
        itemDataAccess.update(item);
        result.success(null, Result.DataSource.LOCAL);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserManager.isLoggedIn()) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            String url = Config.API + Config.USER + Config.PROGRESSIONS + item.id;

            RequestBody body = RequestBody.create(null, new byte[]{});

            Response response = new ApiRequest(url).put(body).execute();
            if (response.isSuccessful()) {
                response.close();

                if (Config.DEBUG) Log.i(TAG, "Progression updated");
                if (result != null) {
                    result.success(null, Result.DataSource.NETWORK);
                }
            } else {
                if (Config.DEBUG) Log.w(TAG, "Progression not updated");
                if (result != null) {
                    result.error(Result.ErrorCode.NO_RESULT);
                }
            }
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
