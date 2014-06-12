package de.xikolo.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.dataaccess.JsonRequest;
import de.xikolo.model.Course;
import de.xikolo.util.BuildType;
import de.xikolo.util.Config;
import de.xikolo.util.Network;
import de.xikolo.util.Toaster;

public abstract class CourseManager {

    public static final String TAG = CourseManager.class.getSimpleName();

    private Context mContext;

    public CourseManager(Context context) {
        super();
        this.mContext = context;
    }

    public void requestCourses(boolean cache) {
        if (BuildConfig.buildType == BuildType.DEBUG)
            Log.i(TAG, "requestCourses() called | cache " + cache);

        Type type = new TypeToken<List<Course>>() {
        }.getType();
        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_COURSES, type, mContext) {
            @Override
            public void onRequestReceived(Object o) {
                if (o != null) {
                    List<Course> courses = (List<Course>) o;
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.i(TAG, "Courses received (" + courses.size() + ")");
                    onCoursesRequestReceived(courses);
                } else {
                    if (BuildConfig.buildType == BuildType.DEBUG)
                        Log.w(TAG, "No Courses received");
                    onCoursesRequestCancelled();
                    Toaster.show(mContext, mContext.getString(R.string.toast_no_courses)
                            + " " + mContext.getString(R.string.toast_no_network));
                }
            }

            @Override
            public void onRequestCancelled() {
                if (BuildConfig.buildType == BuildType.DEBUG)
                    Log.w(TAG, "Courses Request cancelled");
                onCoursesRequestCancelled();
            }
        };
        request.setCache(cache);
        if (!Network.isOnline(mContext) && cache) {
            request.setCacheOnly(true);
        }
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public abstract void onCoursesRequestReceived(List<Course> courses);

    public abstract void onCoursesRequestCancelled();

}
