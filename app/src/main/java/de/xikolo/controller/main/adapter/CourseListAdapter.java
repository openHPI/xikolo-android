package de.xikolo.controller.main.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.xikolo.R;
import de.xikolo.controller.helper.ImageLoaderController;
import de.xikolo.controller.main.CourseListFragment;
import de.xikolo.data.entities.Course;
import de.xikolo.util.DateUtil;
import de.xikolo.util.DisplayUtil;
import de.xikolo.util.LanguageUtil;

public class CourseListAdapter extends BaseAdapter {

    public static final String TAG = CourseListAdapter.class.getSimpleName();

    private List<Course> mCourses;

    private Activity mActivity;
    private OnCourseButtonClickListener mCallback;

    public CourseListAdapter(Activity activity, OnCourseButtonClickListener callback) {
        this.mCourses = new ArrayList<Course>();
        this.mActivity = activity;
        this.mCallback = callback;
    }

    public void updateCourses(List<Course> courses) {
        if (courses == null) throw new NullPointerException("Courses can't be null");
        mCourses = courses;
        this.notifyDataSetChanged();
    }

    public void clear() {
        mCourses.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCourses.size();
    }

    @Override
    public Object getItem(int i) {
        return mCourses.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
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

        final Course course = mCourses.get(i);

        Date dateBegin = DateUtil.parse(course.available_from);
        Date dateEnd = DateUtil.parse(course.available_to);

        DateFormat dateOut;
        if (DisplayUtil.is7inchTablet(mActivity)) {
            dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
        } else {
            dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        }

        if (dateBegin != null && dateEnd != null) {
            holder.date.setText(dateOut.format(dateBegin) + " - " + dateOut.format(dateEnd));
        } else if (dateBegin != null) {
            holder.date.setText(dateOut.format(dateBegin));
        } else {
            holder.date.setText("");
        }

        holder.title.setText(course.name);
        holder.teacher.setText(course.lecturer);
        holder.language.setText(LanguageUtil.languageForCode(mActivity, course.language));

        ImageLoaderController.loadCourseImage(course.visual_url, holder.img, holder.container);

        if (course.is_enrolled && DateUtil.nowIsAfter(course.available_from)) {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onEnterButtonClicked(course);
                }
            });
            holder.enroll.setText(mActivity.getString(R.string.btn_enter_course));
            holder.enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
            holder.enroll.setText(mActivity.getString(R.string.btn_enroll_me));
            holder.enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onEnrollButtonClicked(course);
                }
            });
        }

        if (course.is_enrolled && !DateUtil.nowIsAfter(course.available_from)) {
            holder.enroll.setText(mActivity.getString(R.string.btn_starts_soon));
            holder.enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onDetailButtonClicked(course);
                }
            });
        }

        return rowView;
    }

    public interface OnCourseButtonClickListener {

        public void onEnrollButtonClicked(Course course);

        public void onEnterButtonClicked(Course course);

        public void onDetailButtonClicked(Course course);

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
