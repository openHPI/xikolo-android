package de.xikolo.openhpi.manager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.dataaccess.JsonRequest;
import de.xikolo.openhpi.model.Course;
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

        JsonRequest request = new JsonRequest(mContext.getString(R.string.url_courses), Courses.class, this, mContext);
        request.execute();
    }

    @Override
    public void onJsonReceived(Object o) {
        if (o != null) {
            Courses courses = (Courses) o;

            if (Config.DEBUG) {
                Log.i(TAG, "Courses received");
                for (Course course : courses.getCourses()) {
                    Log.i(TAG, "Title: " + course.title);
                }
            }

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
