package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.HttpRequest;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

public class UpdateProgressionJob extends Job {

    public static final String TAG = UpdateProgressionJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<Void> result;
    private Item item;
    private Module module;
    private ItemDataAccess itemDataAccess;

    public UpdateProgressionJob(Result<Void> result, Module module, Item item, ItemDataAccess itemDataAccess) {
        super(new Params(Priority.LOW));
        id = jobCounter.incrementAndGet();

        this.result = result;
        this.item = item;
        this.module = module;
        this.itemDataAccess = itemDataAccess;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | item.id " + item.id);
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else if (!NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_NETWORK);
        } else {
            String url = Config.API + Config.USER + Config.PROGRESSIONS + item.id;

            HttpRequest request = new HttpRequest(url);
            request.setMethod(Config.HTTP_PUT);
            request.setToken(UserModel.getToken(GlobalApplication.getInstance()));
            request.setCache(false);

            Object o = request.getResponse();
            if (o != null) {
                if (Config.DEBUG) Log.i(TAG, "Progression updated");
                item.progress.visited = true;
                itemDataAccess.updateItem(module, item);
                result.success(null, Result.DataSource.NETWORK);
            } else {
                if (Config.DEBUG) Log.w(TAG, "Progression not updated");
                result.error(Result.ErrorCode.NO_RESULT);
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
