package de.xikolo.jobs.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import de.xikolo.models.Item;
import de.xikolo.models.LtiExercise;
import de.xikolo.models.PeerAssessment;
import de.xikolo.models.Quiz;
import de.xikolo.models.RichText;
import de.xikolo.models.Video;
import de.xikolo.models.base.Sync;
import io.realm.Realm;

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
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    protected void syncItemContent(Item.JsonModel item) {
        syncItemContent(new Item.JsonModel[] {item});
    }

    protected void syncItemContent(Item.JsonModel[] items) {
        Sync.Included.with(RichText.class, items)
                .saveOnly()
                .run();
        Sync.Included.with(Quiz.class, items)
                .saveOnly()
                .run();
        Sync.Included.with(PeerAssessment.class, items)
                .saveOnly()
                .run();
        Sync.Included.with(LtiExercise.class, items)
                .saveOnly()
                .run();
        Sync.Included.with(Video.class, items)
                .saveOnly()
                .setBeforeCommitCallback(new Sync.BeforeCommitCallback<Video>() {
                    @Override
                    public void beforeCommit(Realm realm, Video model) {
                        Video localVideo = realm.where(Video.class).equalTo("id", model.id).findFirst();
                        if (localVideo != null) model.progress = localVideo.progress;
                    }
                })
                .run();
    }

}
