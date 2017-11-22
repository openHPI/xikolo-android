package de.xikolo.jobs.base;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.models.Item;
import de.xikolo.models.LtiExercise;
import de.xikolo.models.PeerAssessment;
import de.xikolo.models.Quiz;
import de.xikolo.models.RichText;
import de.xikolo.models.Video;
import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import moe.banana.jsonapi2.Document;
import moe.banana.jsonapi2.Resource;
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

    protected static <S extends RealmModel, T extends Resource & RealmAdapter<S>> void syncData(final T item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item.convertToRealmObject());
            }
        });
        realm.close();
    }

    protected static <S extends RealmModel, T extends Resource & RealmAdapter<S>> void syncData(final Class<S> clazz, final T[] items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<String> ids = new ArrayList<>();

                for (T item : items) {
                    realm.copyToRealmOrUpdate(item.convertToRealmObject());
                    ids.add(item.getId());
                }

                RealmQuery<S> deleteQuery = realm.where(clazz);
                if (ids.size() > 0) {
                    deleteQuery.not().in("id", ids.toArray(new String[0]));
                }
                deleteQuery.findAll().deleteAllFromRealm();
            }
        });
        realm.close();
    }

    protected static <S extends RealmModel> void syncIncluded(final Class<S> clazz, final Resource[] items, final BeforeCommitCallback<S> beforeCommitCallback) {
        if (items.length > 0) {
            syncIncluded(clazz, items[0].getDocument(), beforeCommitCallback);
        }
    }

    protected static <S extends RealmModel> void syncIncluded(final Class<S> clazz, final Resource item, final BeforeCommitCallback<S> beforeCommitCallback) {
        if (item != null) {
            syncIncluded(clazz, item.getDocument(), beforeCommitCallback);
        }
    }

    protected static <S extends RealmModel> void syncIncluded(final Class<S> clazz, final Document<?> document, final BeforeCommitCallback<S> beforeCommitCallback) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<String> ids = new ArrayList<>();

                for (Resource resource : document.getIncluded()) {
                    if (resource instanceof RealmAdapter) {
                        RealmAdapter adapter = (RealmAdapter) resource;
                        RealmModel model = adapter.convertToRealmObject();
                        if (model.getClass() == clazz) {
                            if (beforeCommitCallback != null) {
                                beforeCommitCallback.beforeCommit(realm, (S) model);
                            }
                            realm.copyToRealmOrUpdate(model);
                            ids.add(resource.getId());
                        }
                    }
                }

                RealmQuery<S> deleteQuery = realm.where(clazz);
                if (ids.size() > 0) {
                    deleteQuery.not().in("id", ids.toArray(new String[0]));
                }
                deleteQuery.findAll().deleteAllFromRealm();
            }
        });
        realm.close();
    }

    public interface BeforeCommitCallback<S extends RealmModel>  {
        void beforeCommit(Realm realm, S model);
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
