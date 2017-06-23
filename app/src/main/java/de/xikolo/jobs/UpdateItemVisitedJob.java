package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.models.Item;
import de.xikolo.network.ApiService;
import de.xikolo.config.Config;
import io.realm.Realm;
import retrofit2.Response;

public class UpdateItemVisitedJob extends BaseJob {

    public static final String TAG = UpdateItemVisitedJob.class.getSimpleName();

    private String itemId;

    public UpdateItemVisitedJob(String itemId) {
        super(new Params(PRIORITY_LOW).requireNetwork().persist(), null);

        this.itemId = itemId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | item.id " + itemId);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Item item = realm.where(Item.class).equalTo("id", itemId).findFirst();
                item.visited = true;
            }
        });
        realm.close();
    }

    @Override
    public void onRun() throws Throwable {
        if (UserManager.isAuthorized()) {
            Item.JsonModel model = new Item.JsonModel();
            model.setId(itemId);
            model.visited = true;

            final Response<Item.JsonModel> response = ApiService.getInstance().updateItem(UserManager.getToken(), itemId, model).execute();

            if (response.isSuccessful()) {
                if (Config.DEBUG) Log.i(TAG, "Item visit successfully updated");

                if (callback != null) callback.onSuccess();
            } else {
                if (Config.DEBUG) Log.e(TAG, "Error while updating item visit");
                if (callback != null) callback.onError(JobCallback.ErrorCode.ERROR);
            }
        } else {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_AUTH);
        }
    }

}
