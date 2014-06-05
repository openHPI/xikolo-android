package de.xikolo.controller.fragments.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import de.xikolo.controller.fragments.ContentFragment;
import de.xikolo.controller.fragments.WebViewFragment;
import de.xikolo.model.Course;
import de.xikolo.model.Enrollment;
import de.xikolo.util.Display;

public class CoursesListAdapter extends BaseAdapter {

    public static final String TAG = CoursesListAdapter.class.getSimpleName();

    private List<Course> mCourses;
    private List<Enrollment> mEnrollments;

    private Activity mContext;

    private OnEnrollButtonClickListener mCallback;

    public CoursesListAdapter(Activity context, OnEnrollButtonClickListener callback) {
        this.mContext = context;
        this.mCourses = new ArrayList<Course>();
        this.mEnrollments = new ArrayList<Enrollment>();
        this.mCallback = callback;
    }

    public void updateCourses(List<Course> courses) {
        this.mCourses = courses;
        this.notifyDataSetChanged();
    }

    public void updateEnrollments(List<Enrollment> enrolls) {
        this.mEnrollments = enrolls;
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

        final Course course = (Course) getItem(i);

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
        if (Display.is7inchTablet(mContext)) {
            dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
        } else {
            dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ContentFragment.OnFragmentInteractionListener) mContext)
                        .attachFragment(WebViewFragment.newInstance(course.url, true, course.name));
            }
        });

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
            holder.enroll.setText(mContext.getString(R.string.btn_enter_course));
            holder.enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.enroll.setClickable(false);
                    holder.enroll.setText("...");
                    mCallback.onEnterButtonClicked(course.id);
                }
            });
        } else {
            holder.enroll.setText(mContext.getString(R.string.btn_enroll_me));
            holder.enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.enroll.setClickable(false);
                    holder.enroll.setText("...");
                    mCallback.onEnrollButtonClicked(course.id);
                }
            });
        }

        if (course.locked) {
            holder.enroll.setText(mContext.getString(R.string.btn_starts_soon));
            holder.enroll.setClickable(false);
        }

        return rowView;
    }

    public interface OnEnrollButtonClickListener {

        public void onEnrollButtonClicked(String id);

        public void onEnterButtonClicked(String id);

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
