package de.xikolo.openhpi.controller.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.model.Course;
import de.xikolo.openhpi.model.Courses;

public class CoursesListAdapter extends BaseAdapter {

    private Courses mCourses;

    private Activity mContext;

    public CoursesListAdapter(Activity context) {
        this.mContext = context;
        this.mCourses = new Courses();
    }

    public void update(Courses courses) {
        this.mCourses = courses;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCourses.getCourses().size();
    }

    @Override
    public Object getItem(int i) {
        return mCourses.getCourses().get(i);
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
            rowView = inflater.inflate(R.layout.courses_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.textView);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();

        Course course = (Course) getItem(i);
        holder.text.setText(course.title);

        return rowView;
    }

    static class ViewHolder {
        TextView text;
    }

}
