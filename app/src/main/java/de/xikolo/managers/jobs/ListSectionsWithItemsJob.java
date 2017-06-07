package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.network.ApiService;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class ListSectionsWithItemsJob extends BaseJob {

    public static final String TAG = ListSectionsWithItemsJob.class.getSimpleName();

    private String courseId;

    public ListSectionsWithItemsJob(String courseId, JobCallback callback) {
        super(new Params(Priority.MID), callback);

        this.courseId = courseId;
    }

    @Override
    public void onAdded() {
        if (Config.DEBUG) Log.i(TAG, TAG + " added | course.id " + courseId);
    }

    @Override
    public void onRun() throws Throwable {
        if (NetworkUtil.isOnline()) {
            if (UserManager.isAuthorized()) {
                final Response<Section.JsonModel[]> response = ApiService.getInstance().listSectionsWithItemsForCourse(UserManager.getToken(), courseId).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG)
                        Log.i(TAG, "Sections received (" + response.body().length + ")");

                    if (callback != null) callback.onSuccess();

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            for (Section.JsonModel sectionModel : response.body()) {
                                realm.copyToRealmOrUpdate(sectionModel.convertToRealmObject());

                                if (sectionModel.items != null && sectionModel.items.get(sectionModel.getContext()) != null) {
                                    for (Item.JsonModel itemModel : sectionModel.items.get(sectionModel.getContext())) {
                                        Item item = itemModel.convertToRealmObject();
                                        item.sectionId = sectionModel.getId();
                                        realm.copyToRealmOrUpdate(item);
                                    }
                                }
                            }
                        }
                    });
                    realm.close();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching section list");
                    if (callback != null) callback.onError(JobCallback.ErrorCode.ERROR);
                }
            } else {
                if (callback != null) callback.onError(JobCallback.ErrorCode.NO_AUTH);
            }
        } else {
            if (callback != null) callback.onError(JobCallback.ErrorCode.NO_NETWORK);
        }
    }

}