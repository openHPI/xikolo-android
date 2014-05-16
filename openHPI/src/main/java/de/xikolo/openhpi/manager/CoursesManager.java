package de.xikolo.openhpi.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.xikolo.openhpi.dataaccess.JsonRequest;
import de.xikolo.openhpi.model.Course;
import de.xikolo.openhpi.util.Config;

public class CoursesManager implements JsonRequest.OnJsonReceivedListener {

    public static final String TAG = CoursesManager.class.getSimpleName();

    private Context mContext;

    private OnCoursesReceivedListener mCallback;

    public CoursesManager(OnCoursesReceivedListener mCallback, Context context) {
        super();
        this.mContext = context;
        this.mCallback = mCallback;
    }

    public void requestCourses() {
        if (Config.DEBUG)
            Log.i(TAG, "requestCourses() called");

        Type type = new TypeToken<List<Course>>() {
        }.getType();
        JsonRequest request = new JsonRequest(Config.API_SAP + Config.PATH_COURSES, type, this, mContext);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onJsonReceived(Object o) {
        if (o != null) {
            List<Course> courses = (List<Course>) o;
            Log.i(TAG, "Courses received (" + courses.size() + ")");
            mCallback.onCoursesReceived(courses);
        } else {
            if (Config.DEBUG)
                Log.w(TAG, "No Courses received");

            mCallback.onCoursesRequestCancelled();
        }
    }

    @Override
    public void onJsonRequestCancelled() {
        if (Config.DEBUG)
            Log.w(TAG, "Courses Request cancelled");

        mCallback.onCoursesRequestCancelled();
    }

    public interface OnCoursesReceivedListener {

        public void onCoursesReceived(List<Course> courses);

        public void onCoursesRequestCancelled();

    }
}
