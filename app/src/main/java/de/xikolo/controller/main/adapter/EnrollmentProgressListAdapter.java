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

public class EnrollmentProgressListAdapter extends BaseAdapter {

    public static final String TAG = EnrollmentProgressListAdapter.class.getSimpleName();

    private List<Course> mCourses;
    private List<Enrollment> mEnrollments;

    private Activity mContext;

    public EnrollmentProgressListAdapter(Activity context) {
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
        notifyDataSetChanged();
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
            rowView = inflater.inflate(R.layout.item_enrollment_progress, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.percentage = (TextView) rowView.findViewById(R.id.textPercentage);
            viewHolder.progressItems = (ProgressBar) rowView.findViewById(R.id.progress);
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

        holder.count.setText(course.progress.items.count_visited + " " +
                mContext.getString(R.string.of) + " " + course.progress.items.count_available + " " +
                mContext.getString(R.string.visited));

        int percentage = getPercentage(course.progress.items.count_visited,
                course.progress.items.count_available);

        holder.percentage.setText(percentage + "%");

        holder.progressItems.setProgress(percentage);

        return rowView;
    }

    private int getPercentage(int state, int max) {
        int percentage;
        if (max > 0) {
            percentage = (int) (state / (max / 100.));
        } else {
            percentage = 100;
        }
        return percentage;
    }

    static class ViewHolder {
        TextView title;
        TextView percentage;
        ProgressBar progressItems;
        TextView count;
    }

}
