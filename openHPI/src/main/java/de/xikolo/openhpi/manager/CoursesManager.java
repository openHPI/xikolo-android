package de.xikolo.openhpi.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import de.xikolo.openhpi.dataaccess.JsonRequest;
import de.xikolo.openhpi.model.Courses;
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

        JsonRequest request = new JsonRequest("https://openhpi.de/feeds/courses", Courses.class, this, mContext);
        request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onJsonReceived(Object o) {
        if (o != null) {
            Courses courses = (Courses) o;
            Log.i(TAG, "Courses received (" + courses.getCourses().size() + ")");
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

        public void onCoursesReceived(Courses courses);

        public void onCoursesRequestCancelled();

    }
}
