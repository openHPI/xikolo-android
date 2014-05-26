package de.xikolo.openhpi.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.dataaccess.JsonRequest;
import de.xikolo.openhpi.model.Course;
import de.xikolo.openhpi.util.Config;
import de.xikolo.openhpi.util.Network;
import de.xikolo.openhpi.util.Toaster;

public abstract class CoursesManager {

    public static final String TAG = CoursesManager.class.getSimpleName();

    private Context mContext;

    public CoursesManager(Context context) {
        super();
        this.mContext = context;
    }

    public void requestCourses(boolean cache) {
        if (Config.DEBUG)
            Log.i(TAG, "requestCourses() called; cache " + cache);

        Type type = new TypeToken<List<Course>>() {
        }.getType();
        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_COURSES, type, mContext) {
            @Override
            public void onJsonRequestReceived(Object o) {
                if (o != null) {
                    List<Course> courses = (List<Course>) o;
                    Log.i(TAG, "Courses received (" + courses.size() + ")");
                    if (Config.DEBUG)
                        onCoursesRequestReceived(courses);
                } else {
                    if (Config.DEBUG)
                        Log.w(TAG, "No Courses received");
                    onCoursesRequestCancelled();
                    Toaster.show(mContext, mContext.getString(R.string.toast_no_courses)
                            + " " + mContext.getString(R.string.toast_no_network));
                }
            }

            @Override
            public void onJsonRequestCancelled() {
                if (Config.DEBUG)
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
