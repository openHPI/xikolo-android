package de.xikolo.openhpi.controller.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.controller.fragments.ContentFragment;
import de.xikolo.openhpi.controller.fragments.WebViewFragment;
import de.xikolo.openhpi.model.Course;
import de.xikolo.openhpi.util.DisplayConfig;

public class CoursesListAdapter extends BaseAdapter {

    public static final String TAG = CoursesListAdapter.class.getSimpleName();

    private List<Course> mCourses;

    private Activity mContext;

    public CoursesListAdapter(Activity context) {
        this.mContext = context;
        this.mCourses = new ArrayList<Course>();
    }

    public void update(List<Course> courses) {
        this.mCourses = courses;
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
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();

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
        if (DisplayConfig.is7inchTablet(mContext)) {
            dateOut = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
        } else {
            dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ContentFragment.OnFragmentInteractionListener) mContext)
                        .attachLowerFragment(WebViewFragment.newInstance(course.url, true, course.name));
            }
        });

        holder.title.setText(course.name);
        holder.teacher.setText(course.lecturer);
        holder.date.setText(dateOut.format(dateBegin) + " - " + dateOut.format(dateEnd));
        holder.language.setText(course.language);
        ImageLoader.getInstance().displayImage(course.visual_url, holder.img);

        return rowView;
    }

    static class ViewHolder {
        ViewGroup container;
        TextView title;
        TextView teacher;
        TextView date;
        TextView language;
        ImageView img;
    }

}
