package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class GetItemWithContentJob extends BaseJob {

    public static final String TAG = GetItemWithContentJob.class.getSimpleName();

    private String itemId;

    public GetItemWithContentJob(JobCallback callback, String itemId) {
        super(new Params(PRIORITY_HIGH), callback);

        this.itemId = itemId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | item.id " + itemId);
        }
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {
                final Response<Item.JsonModel> response = ApiService.getInstance().getItemWithContent(
                        UserManager.getTokenAsHeader(),
                        itemId
                ).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG)
                        Log.i(TAG, "Item received");

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Item.JsonModel itemModel = response.body();
                            Item item = itemModel.convertToRealmObject();
                            realm.copyToRealmOrUpdate(item);
                            extractItemContent(realm, item, itemModel.getContext(), itemModel.content.get());
                        }
                    });
                    realm.close();

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching section list");
                    if (callback != null) callback.error(JobCallback.ErrorCode.ERROR);
                }
            } else {
                if (callback != null) callback.error(JobCallback.ErrorCode.NO_AUTH);
            }
        } else {
            if (callback != null) callback.error(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}
