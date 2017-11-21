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

public class ListItemsWithContentForSectionJob extends BaseJob {

    public static final String TAG = ListItemsWithContentForSectionJob.class.getSimpleName();

    private String sectionId;

    public ListItemsWithContentForSectionJob(JobCallback callback, String sectionId) {
        super(new Params(PRIORITY_HIGH), callback);

        this.sectionId = sectionId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) {
            Log.i(TAG, TAG + " added | section.id " + sectionId);
        }
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {
                final Response<Item.JsonModel[]> response = ApiService.getInstance().listItemsWithContentForSection(
                        UserManager.getTokenAsHeader(),
                        sectionId
                ).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG)
                        Log.i(TAG, "Items received");

                    Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (Item.JsonModel itemModel : response.body()) {
                                    Item item = itemModel.convertToRealmObject();
                                    realm.copyToRealmOrUpdate(item);
                                    extractItemContent(realm, item, itemModel.getDocument(), itemModel.content.get());
                                }
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
