package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.CourseProgress;
import de.xikolo.models.SectionProgress;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class GetCourseProgressWithSectionsJob extends BaseJob {

    public static final String TAG = GetCourseProgressWithSectionsJob.class.getSimpleName();

    private String courseId;

    public GetCourseProgressWithSectionsJob(String courseId, JobCallback callback) {
        super(new Params(PRIORITY_MID), callback);

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
                final Response<CourseProgress.JsonModel> response = ApiService.getInstance().getCourseProgressWithSections(
                        UserManager.getTokenAsHeader(),
                        courseId
                ).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "Course progress received");

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            CourseProgress.JsonModel cpModel = response.body();
                            realm.copyToRealmOrUpdate(cpModel.convertToRealmObject());

                            if (cpModel.sectionProgresses != null && cpModel.sectionProgresses.get(cpModel.getContext()) != null) {
                                for (SectionProgress.JsonModel spModel : cpModel.sectionProgresses.get(cpModel.getContext())) {
                                    SectionProgress cp = spModel.convertToRealmObject();
                                    cp.courseProgressId = cpModel.getId();
                                    realm.copyToRealmOrUpdate(cp);
                                }
                            }
                        }
                    });
                    realm.close();

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching course progress");
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
