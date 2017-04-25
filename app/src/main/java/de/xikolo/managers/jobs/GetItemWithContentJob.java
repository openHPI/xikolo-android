package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.managers.UserManager;
import de.xikolo.models.Item;
import de.xikolo.models.LtiExercise;
import de.xikolo.models.PeerAssessment;
import de.xikolo.models.Quiz;
import de.xikolo.models.RichText;
import de.xikolo.models.Video;
import de.xikolo.network.ApiService;
import de.xikolo.utils.Config;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class GetItemWithContentJob extends BaseJob {

    public static final String TAG = GetItemWithContentJob.class.getSimpleName();

    private String itemId;

    public GetItemWithContentJob(JobCallback callback, String itemId) {
        super(new Params(Priority.HIGH), callback);

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
                final Response<Item.JsonModel> response = ApiService.getInstance().getItemWithContent(UserManager.getToken(), itemId).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG)
                        Log.i(TAG, "Item received");

                    if (callback != null) callback.onSuccess();

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Item item = response.body().convertToRealmObject();
                            realm.copyToRealmOrUpdate(item);

                            switch (item.type) {
                                case Item.TYPE_TEXT:
                                    RichText.JsonModel rtModel = response.body().getContext().find(response.body().content.get());
                                    RichText rt = rtModel.convertToRealmObject();
                                    rt.itemId = itemId;
                                    realm.copyToRealmOrUpdate(rt);
                                    break;
                                case Item.TYPE_QUIZ:
                                    Quiz.JsonModel quizModel = response.body().getContext().find(response.body().content.get());
                                    Quiz quiz = quizModel.convertToRealmObject();
                                    quiz.itemId = itemId;
                                    realm.copyToRealmOrUpdate(quiz);
                                    break;
                                case Item.TYPE_VIDEO:
                                    Video.JsonModel videoModel = response.body().getContext().find(response.body().content.get());
                                    Video video = videoModel.convertToRealmObject();
                                    video.itemId = itemId;
                                    realm.copyToRealmOrUpdate(video);
                                    break;
                                case Item.TYPE_LTI:
                                    LtiExercise.JsonModel ltiModel = response.body().getContext().find(response.body().content.get());
                                    LtiExercise lti = ltiModel.convertToRealmObject();
                                    lti.itemId = itemId;
                                    realm.copyToRealmOrUpdate(lti);
                                    break;
                                case Item.TYPE_PEER:
                                    PeerAssessment.JsonModel peerModel = response.body().getContext().find(response.body().content.get());
                                    PeerAssessment peer = peerModel.convertToRealmObject();
                                    peer.itemId = itemId;
                                    realm.copyToRealmOrUpdate(peer);
                                    break;
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
