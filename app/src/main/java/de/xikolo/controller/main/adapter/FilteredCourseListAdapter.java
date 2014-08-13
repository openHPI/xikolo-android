package de.xikolo.controller.main.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.xikolo.R;
import de.xikolo.controller.main.CourseListFragment;
import de.xikolo.entities.Course;
import de.xikolo.entities.Enrollment;
import de.xikolo.util.DisplayUtil;

public class FilteredCourseListAdapter extends CourseListAdapter {

    public static final String TAG = FilteredCourseListAdapter.class.getSimpleName();

    private List<Course> mCourses;
    private List<Enrollment> mEnrollments;

    private Activity mContext;

    private String mFilter;

    private OnCourseButtonClickListener mCallback;

    public FilteredCourseListAdapter(Activity context, OnCourseButtonClickListener callback, String filter) {
        this.mContext = context;
        this.mCourses = new ArrayList<Course>();
        this.mEnrollments = new ArrayList<Enrollment>();
        this.mCallback = callback;
        this.mFilter = filter;
    }

    @Override
    public void updateCourses(List<Course> courses) {
        if (courses == null)
            throw new NullPointerException("Courses can't be null");
        this.mCourses = courses;
        this.notifyDataSetChanged();
    }

    @Override
    public void updateEnrollments(List<Enrollment> enrolls) {
        if (enrolls == null)
            throw new NullPointerException("Enrollments can't be null");
        this.mEnrollments = enrolls;
        this.notifyDataSetChanged();
    }

    @Override
    public void clear() {
        this.mCourses.clear();
        this.mEnrollments.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int size = 0;
        if (mFilter.equals(CourseListFragment.FILTER_MY)) {
            if (mCourses.size() >= mEnrollments.size()) {
                size = mEnrollments.size();
            }
        } else if (mFilter.equals(CourseListFragment.FILTER_ALL)) {
            size = mCourses.size();
        }
        return size;
    }

    @Override
    public Object getItem(int i) {
        Object o = null;
        if (mFilter.equals(CourseListFragment.FILTER_MY)) {
            o = mEnrollments.get(i);
        } else if (mFilter.equals(CourseListFragment.FILTER_ALL)) {
            o = mCourses.get(i);
        }
        return o;
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
            rowView = inflater.inflate(R.layout.item_courses, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.container = (ViewGroup) rowView.findViewById(R.id.container);
            viewHolder.title = (TextView) rowView.findViewById(R.id.textTitle);
            viewHolder.teacher = (TextView) rowView.findViewById(R.id.textTeacher);
            viewHolder.date = (TextView) rowView.findViewById(R.id.textDate);
            viewHolder.language = (TextView) rowView.findViewById(R.id.textLanguage);
            viewHolder.img = (ImageView) rowView.findViewById(R.id.imageView);
            viewHolder.enroll = (Button) rowView.findViewById(R.id.btnEnroll);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        int c_id = 0;

        if (mFilter.equals(CourseListFragment.FILTER_MY)) {
            final Enrollment enrollment = (Enrollment) getItem(i);
            for (Course c : mCourses) {
                if (enrollment.course_id.equals(c.id)) {
                    c_id = mCourses.indexOf(c);
                }
            }
        } else if (mFilter.equals(CourseListFragment.FILTER_ALL)) {
            c_id = i;
        }
        final Course course = mCourses.get(c_id);

        SimpleDateFormat dateIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateBegin = new Date();
        Date dateEnd = new Date();
        try {
            dateBegin = dateIn.parse(course.available_from);
        } catch (ParseException e) {
            Log.w(TAG, "Failed parsing " + course.available_from, e);
        }
        try {
            dateEnd = dateIn.parse(course.available_to);
        } catch (ParseException e) {
            Log.w(TAG, "Failed parsing " + course.available_to, e);
        }
        DateFormat dateOut;
        if (DisplayUtil.is7inchTablet(mContext)) {
            dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
        } else {
            dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        }

        holder.title.setText(course.name);
        holder.teacher.setText(course.lecturer);
        holder.date.setText(dateOut.format(dateBegin) + " - " + dateOut.format(dateEnd));
        holder.language.setText(course.language);
        ImageLoader.getInstance().displayImage(course.visual_url, holder.img);

        boolean isEnrolled = false;
        if (mEnrollments != null) {
            for (Enrollment enroll : mEnrollments) {
                if (enroll.course_id.equals(course.id)) {
                    isEnrolled = true;
                }
            }
        }
        if (isEnrolled) {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onEnterButtonClicked(course);
                }
            });
            holder.enroll.setText(mContext.getString(R.string.btn_enter_course));
            holder.enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.enroll.setClickable(false);
                    mCallback.onEnterButtonClicked(course);
                }
            });
        } else {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onDetailButtonClicked(course);
                }
            });
            holder.enroll.setText(mContext.getString(R.string.btn_enroll_me));
            holder.enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.enroll.setClickable(false);
                    holder.enroll.setText("...");
                    mCallback.onEnrollButtonClicked(course);
                }
            });
        }

        if (course.locked) {
            holder.enroll.setText(mContext.getString(R.string.btn_starts_soon));
            holder.enroll.setClickable(false);
        }

        return rowView;
    }

    static class ViewHolder {
        ViewGroup container;
        TextView title;
        TextView teacher;
        TextView date;
        TextView language;
        ImageView img;
        Button enroll;
    }

}
