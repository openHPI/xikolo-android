package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.models.LtiExercise;
import de.xikolo.models.PeerAssessment;
import de.xikolo.models.Quiz;
import de.xikolo.models.RichText;
import de.xikolo.models.Video;
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

                    if (callback != null) callback.success();

                    Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (Item.JsonModel itemModel : response.body()) {
                                    Item item = itemModel.convertToRealmObject();
                                    realm.copyToRealmOrUpdate(item);

                                    switch (item.type) {
                                        case Item.TYPE_TEXT:
                                            RichText.JsonModel rtModel = itemModel.getContext().find(itemModel.content.get());
                                            RichText rt = rtModel.convertToRealmObject();
                                            rt.itemId = item.id;
                                            realm.copyToRealmOrUpdate(rt);
                                            break;
                                        case Item.TYPE_QUIZ:
                                            Quiz.JsonModel quizModel = itemModel.getContext().find(itemModel.content.get());
                                            Quiz quiz = quizModel.convertToRealmObject();
                                            quiz.itemId = item.id;
                                            realm.copyToRealmOrUpdate(quiz);
                                            break;
                                        case Item.TYPE_VIDEO:
                                            Video.JsonModel videoModel = itemModel.getContext().find(itemModel.content.get());
                                            Video video = videoModel.convertToRealmObject();
                                            video.itemId = item.id;
                                            Video localVideo = realm.where(Video.class).equalTo("id", video.id).findFirst();
                                            if (localVideo != null) {
                                                video.progress = localVideo.progress;
                                            }
                                            realm.copyToRealmOrUpdate(video);
                                            break;
                                        case Item.TYPE_LTI:
                                            LtiExercise.JsonModel ltiModel = itemModel.getContext().find(itemModel.content.get());
                                            LtiExercise lti = ltiModel.convertToRealmObject();
                                            lti.itemId = item.id;
                                            realm.copyToRealmOrUpdate(lti);
                                            break;
                                        case Item.TYPE_PEER:
                                            PeerAssessment.JsonModel peerModel = itemModel.getContext().find(itemModel.content.get());
                                            PeerAssessment peer = peerModel.convertToRealmObject();
                                            peer.itemId = item.id;
                                            realm.copyToRealmOrUpdate(peer);
                                            break;
                                    }
                                }
                            }
                        });
                    realm.close();
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
