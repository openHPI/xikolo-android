package de.xikolo.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.Params;

import de.xikolo.config.Config;
import de.xikolo.jobs.base.BaseJob;
import de.xikolo.jobs.base.JobCallback;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Course;
import de.xikolo.models.Section;
import de.xikolo.network.ApiService;
import de.xikolo.utils.NetworkUtil;
import io.realm.Realm;
import retrofit2.Response;

public class GetCourseWithSectionsJob extends BaseJob {

    public static final String TAG = GetCourseWithSectionsJob.class.getSimpleName();

    private String courseId;

    public GetCourseWithSectionsJob(String courseId, JobCallback callback) {
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

                final Response<Course.JsonModel> response = ApiService.getInstance().getCourseWithSections(
                        UserManager.getTokenAsHeader(),
                        courseId
                ).execute();

                if (response.isSuccessful()) {
                    if (Config.DEBUG) Log.i(TAG, "Course received");

                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Course.JsonModel courseModel = response.body();
                            realm.copyToRealmOrUpdate(courseModel.convertToRealmObject());

                            if (courseModel.sections != null && courseModel.sections.get(courseModel.getContext()) != null) {
                                for (Section.JsonModel sectionModel : courseModel.sections.get(courseModel.getContext())) {
                                    Section section = sectionModel.convertToRealmObject();
                                    section.courseId = courseModel.getId();
                                    realm.copyToRealmOrUpdate(section);
                                }
                            }
                        }
                    });
                    realm.close();

                    if (callback != null) callback.success();
                } else {
                    if (Config.DEBUG) Log.e(TAG, "Error while fetching course");
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
