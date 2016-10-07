package de.xikolo.model.jobs;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

import de.xikolo.GlobalApplication;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.data.database.ItemDataAccess;
import de.xikolo.data.database.VideoDataAccess;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.data.net.ApiRequest;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.model.Result;
import de.xikolo.model.UserModel;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;
import okhttp3.Response;

public class RetrieveItemDetailJob extends Job {

    public static final String TAG = RetrieveItemDetailJob.class.getSimpleName();

    private static final AtomicInteger jobCounter = new AtomicInteger(0);

    private final int id;

    private Result<Item> result;

    private String courseId;
    private String moduleId;
    private String itemId;

    private String itemType;

    public RetrieveItemDetailJob(Result<Item> result, String courseId, String moduleId, String itemId, String itemType) {
        super(new Params(Priority.HIGH));
        id = jobCounter.incrementAndGet();

        if (courseId == null || moduleId == null || itemId == null) {
            throw new WrongParameterException();
        }

        this.result = result;
        this.courseId = courseId;
        this.moduleId = moduleId;
        this.itemId = itemId;

        this.itemType = itemType;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | course.id " + courseId + " | module.id " + moduleId + " | item.id " + itemId + " | itemType " + itemType);
        }
    }

    @Override
    public void onRun() throws Throwable {
        if (!UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            result.error(Result.ErrorCode.NO_AUTH);
        } else {
            VideoDataAccess videoDataAccess = GlobalApplication.getInstance()
                    .getDataAccessFactory().getVideoDataAccess();
            ItemDataAccess itemDataAccess = GlobalApplication.getInstance()
                    .getDataAccessFactory().getItemDataAccess();
            if (itemType.equals(Item.TYPE_VIDEO)) {
                Item item = itemDataAccess.getItem(itemId);
                if (item != null) {
                    item.detail = videoDataAccess.getVideo(itemId);
                    if (item.detail != null) {
                        result.success(item, Result.DataSource.LOCAL);
                    }
                }
            }

            if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
                String url = Config.API + Config.COURSES + courseId + "/"
                            + Config.MODULES + moduleId + "/" + Config.ITEMS + itemId;

                Response response = new ApiRequest(url).execute();
                if (response.isSuccessful()) {
                    Type type = Item.getTypeToken(itemType);
                    Item item = ApiParser.parse(response, type);
                    response.close();

                    if (Config.DEBUG) Log.i(TAG, "ItemDetail received");

                    if (itemType.equals(Item.TYPE_VIDEO)) {
                        itemDataAccess.addOrUpdateItem(moduleId, item);
                        videoDataAccess.addOrUpdateVideo((VideoItemDetail) item.detail);
                        // get local video progress, if available
                        item.detail = videoDataAccess.getVideo(item.id);
                    }

                    result.success(item, Result.DataSource.NETWORK);
                } else {
                    if (Config.DEBUG) Log.w(TAG, "No ItemDetail received");
                    result.error(Result.ErrorCode.NO_RESULT);
                }
            } else {
                if (itemType.equals(Item.TYPE_VIDEO)) {
                    result.warn(Result.WarnCode.NO_NETWORK);
                } else {
                    result.error(Result.ErrorCode.NO_NETWORK);
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
