package de.xikolo.controllers.base;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.models.Course;
import de.xikolo.utils.SectionList;

public abstract class BaseCourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = BaseCourseListAdapter.class.getSimpleName();

    protected SectionList<String, List<Course>> courseList;

    protected OnCourseButtonClickListener callback;

    protected Fragment fragment;

    public void update(SectionList<String, List<Course>> courseList) {
        this.courseList = courseList;
        this.notifyDataSetChanged();
    }

    public boolean isHeader(int position) {
        return courseList.isHeader(position);
    }

    public void clear() {
        courseList.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public interface OnCourseButtonClickListener {

        void onEnrollButtonClicked(String courseId);

        void onContinueButtonClicked(String courseId);

        void onDetailButtonClicked(String courseId);

    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) public ViewGroup layout;
        @BindView(R.id.textTitle) public TextView textTitle;
        @BindView(R.id.textTeacher) public TextView textTeacher;
        @BindView(R.id.textDate) public TextView textDate;
        @BindView(R.id.textLanguage) public TextView textLanguage;
        @BindView(R.id.textDescription) public TextView textDescription;
        @BindView(R.id.imageView) public ImageView image;
        @BindView(R.id.button_course_action) public Button buttonCourseAction;
        @BindView(R.id.button_course_details) public Button buttonCourseDetails;
        @BindView(R.id.textBanner) public TextView textBanner;

        public CourseViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) public ViewGroup container;
        @BindView(R.id.textHeader) public TextView header;

        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
