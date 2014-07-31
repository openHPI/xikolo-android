package de.xikolo.controller.main.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.model.Course;
import de.xikolo.model.Enrollment;

public class EnrollsProgressListAdapter extends BaseAdapter {

    public static final String TAG = EnrollsProgressListAdapter.class.getSimpleName();

    private List<Course> mCourses;
    private List<Enrollment> mEnrollments;

    private Activity mContext;

    private String mFilter;

    public EnrollsProgressListAdapter(Activity context) {
        this.mContext = context;
        this.mCourses = new ArrayList<Course>();
        this.mEnrollments = new ArrayList<Enrollment>();
    }

    public void updateEnrollments(List<Enrollment> enrolls) {
        if (enrolls == null)
            throw new NullPointerException("Enrollments can't be null");
        this.mEnrollments = enrolls;
    }

    public void updateCourses(List<Course> courses) {
        if (courses == null)
            throw new NullPointerException("Courses can't be null");
        this.mCourses = courses;
    }

    @Override
    public int getCount() {
        return mEnrollments.size();
    }

    @Override
    public Object getItem(int i) {
        return mEnrollments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_enrolls_progress, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.progressItems = (ProgressBar) rowView.findViewById(R.id.progressItems);
            viewHolder.count = (TextView) rowView.findViewById(R.id.textCount);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        Enrollment enrollment = (Enrollment) getItem(i);
        Course course = null;
        for (Course c : mCourses) {
            if (enrollment.course_id.equals(c.id)) {
                course = c;
            }
        }

        holder.title.setText(course.name);

        int percentage = (int) (course.progress.items.count_visited
                / (course.progress.items.count_available / 100.));

        holder.count.setText(course.progress.items.count_visited + " " +
                        mContext.getString(R.string.of) + " " + course.progress.items.count_available + " " +
                        mContext.getString(R.string.visited) + " (" + percentage + "%)"
        );

        return rowView;
    }

    public void animateProgress(final Activity activity, final ProgressBar progress, final int percentage) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setProgress(percentage);
                        }
                    });
                }
                final int animTime = (int) (500. / percentage);
                for (int i = 0; i <= percentage; i++) {
                    final int p = i;
                    try {
                        Thread.sleep(animTime);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.setProgress(p);
                            }
                        });
                    } catch (InterruptedException e) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.setProgress(percentage);
                            }
                        });
                    }
                }

            }
        })).start();
    }

    static class ViewHolder {
        TextView title;
        ProgressBar progressItems;
        TextView count;
    }

}
