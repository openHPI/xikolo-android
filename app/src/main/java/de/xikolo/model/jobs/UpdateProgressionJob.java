package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.HttpRequest;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;

public class UpdateProgressionJob extends Job {

    public static final String TAG = UpdateProgressionJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private transient Result<Void> result;
    private Item item;
    private Module module;

    public UpdateProgressionJob(Result<Void> result, Module module, Item item) {
        super(new Params(Priority.LOW).requireNetwork().persist());
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.item = item;
        this.module = module;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | item.id " + item.id);
        Log.d(TAG, "update progression added");

        ItemDataAccess itemDataAccess = GlobalApplication.getInstance()
                .getDataAccessFactory().getItemDataAccess();

        item.progress.visited = true;
        itemDataAccess.updateItem(module.id, item);
        result.success(null, Result.DataSource.LOCAL);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            String url = Config.API + Config.USER + Config.PROGRESSIONS + item.id;

            HttpRequest request = new HttpRequest(url);
            request.setMethod(Config.HTTP_PUT);
            request.setToken(UserModel.getToken(GlobalApplication.getInstance()));
            request.setCache(false);

            Object o = request.getResponse();
            if (o != null) {
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
    protected void onCancel() {
        result.error(Result.ErrorCode.ERROR);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
