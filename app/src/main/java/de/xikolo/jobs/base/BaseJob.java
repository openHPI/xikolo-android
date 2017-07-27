package de.xikolo.jobs.base;

import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.models.Item;
import de.xikolo.models.LtiExercise;
import de.xikolo.models.PeerAssessment;
import de.xikolo.models.Quiz;
import de.xikolo.models.RichText;
import de.xikolo.models.Video;
import io.realm.Realm;
import moe.banana.jsonapi2.Document;
import moe.banana.jsonapi2.ResourceIdentifier;

public abstract class BaseJob extends Job {

    protected static int PRIORITY_LOW = 0;
    protected static int PRIORITY_MID = 500;
    protected static int PRIORITY_HIGH = 1000;

    protected JobCallback callback;

    public BaseJob(Params params, JobCallback callback) {
        super(params);
        this.callback = callback;
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        if (callback != null) callback.error(JobCallback.ErrorCode.CANCEL);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    protected void extractItemContent(Realm realm, Item item, Document<?> document, ResourceIdentifier contentIdentifier) {
        switch (item.type) {
            case Item.TYPE_TEXT:
                RichText.JsonModel rtModel = document.find(contentIdentifier);
                RichText rt = rtModel.convertToRealmObject();
                rt.itemId = item.id;
                realm.copyToRealmOrUpdate(rt);
                break;
            case Item.TYPE_QUIZ:
                Quiz.JsonModel quizModel = document.find(contentIdentifier);
                Quiz quiz = quizModel.convertToRealmObject();
                quiz.itemId = item.id;
                realm.copyToRealmOrUpdate(quiz);
                break;
            case Item.TYPE_VIDEO:
                Video.JsonModel videoModel = document.find(contentIdentifier);
                Video video = videoModel.convertToRealmObject();
                video.itemId = item.id;
                Video localVideo = realm.where(Video.class).equalTo("id", video.id).findFirst();
                if (localVideo != null) {
                    video.progress = localVideo.progress;
                }
                realm.copyToRealmOrUpdate(video);
                break;
            case Item.TYPE_LTI:
                LtiExercise.JsonModel ltiModel = document.find(contentIdentifier);
                LtiExercise lti = ltiModel.convertToRealmObject();
                lti.itemId = item.id;
                realm.copyToRealmOrUpdate(lti);
                break;
            case Item.TYPE_PEER:
                PeerAssessment.JsonModel peerModel = document.find(contentIdentifier);
                PeerAssessment peer = peerModel.convertToRealmObject();
                peer.itemId = item.id;
                realm.copyToRealmOrUpdate(peer);
                break;
        }
    }

}
