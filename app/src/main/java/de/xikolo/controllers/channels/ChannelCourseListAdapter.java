package de.xikolo.controllers.channels;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.main.CourseListAdapter;
import de.xikolo.models.Course;
import de.xikolo.utils.DateUtil;

public class ChannelCourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = ChannelCourseListAdapter.class.getSimpleName();

    private List<Course> courseList;

    private CourseListAdapter.OnCourseButtonClickListener callback;

    private Fragment fragment;

    private int channelColor = 0;

    public ChannelCourseListAdapter(Fragment fragment, CourseListAdapter.OnCourseButtonClickListener callback) {
        this.fragment = fragment;
        this.courseList = new ArrayList<>();
        this.callback = callback;
    }

    public void update(List<Course> courseList) {
        this.courseList = courseList;
        this.notifyDataSetChanged();
    }

    public void clear() {
        courseList.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_list, parent, false);
        return new CourseViewHolder(view);
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CourseViewHolder viewHolder = (CourseViewHolder) holder;

        final Course course = courseList.get(position);

        Context context = App.getInstance();

        viewHolder.textDate.setText(course.getFormattedDate());
        viewHolder.textTitle.setText(course.title);
        viewHolder.textTeacher.setText(course.teachers);
        viewHolder.textLanguage.setText(course.getFormattedLanguage());

        if (course.teachers == null || "".equals(course.teachers)) {
            viewHolder.textTeacher.setVisibility(View.GONE);
        } else {
            viewHolder.textTeacher.setVisibility(View.VISIBLE);
        }

        viewHolder.textDescription.setText(course.shortAbstract);
        viewHolder.textDescription.setVisibility(View.VISIBLE);

        if (DateUtil.nowIsBetween(course.startDate, course.endDate)) {
            viewHolder.textBanner.setVisibility(View.VISIBLE);
            viewHolder.textBanner.setText(context.getText(R.string.banner_running));
            viewHolder.textBanner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_green));
        } else {
            viewHolder.textBanner.setVisibility(View.GONE);
        }


        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            viewHolder.textBanner.setVisibility(View.GONE);
        }

        GlideApp.with(fragment).load(course.imageUrl).into(viewHolder.image);

        viewHolder.buttonCourseAction.setTextColor(channelColor);
        viewHolder.buttonCourseDetails.setTextColor(channelColor);

        viewHolder.buttonCourseAction.setEnabled(true);

        viewHolder.buttonCourseDetails.setVisibility(View.VISIBLE);
        viewHolder.buttonCourseDetails.setOnClickListener(v -> callback.onDetailButtonClicked(course.id));

        if (course.isEnrolled() && course.accessible) {
            viewHolder.layout.setOnClickListener(v -> callback.onContinueButtonClicked(course.id));

            viewHolder.buttonCourseAction.setText(context.getString(R.string.btn_continue_course));
            viewHolder.buttonCourseAction.setOnClickListener(v -> callback.onContinueButtonClicked(course.id));

            viewHolder.buttonCourseDetails.setVisibility(View.GONE);

        } else if (course.isEnrolled() && !course.accessible) {
            viewHolder.layout.setOnClickListener(v -> callback.onDetailButtonClicked(course.id));

            viewHolder.buttonCourseAction.setText(context.getString(R.string.btn_starts_soon));
            viewHolder.buttonCourseAction.setEnabled(false);
            viewHolder.buttonCourseAction.setClickable(false);

        } else {
            viewHolder.layout.setOnClickListener(v -> callback.onDetailButtonClicked(course.id));

            viewHolder.buttonCourseAction.setText(context.getString(R.string.btn_enroll));
            viewHolder.buttonCourseAction.setOnClickListener(v -> callback.onEnrollButtonClicked(course.id));
        }
    }

    public void setButtonColor(int color){
        this.channelColor = color;
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container) ViewGroup layout;
        @BindView(R.id.textTitle) TextView textTitle;
        @BindView(R.id.textTeacher) TextView textTeacher;
        @BindView(R.id.textDate) TextView textDate;
        @BindView(R.id.textLanguage) TextView textLanguage;
        @BindView(R.id.textDescription) TextView textDescription;
        @BindView(R.id.imageView) ImageView image;
        @BindView(R.id.button_course_action) Button buttonCourseAction;
        @BindView(R.id.button_course_details) Button buttonCourseDetails;
        @BindView(R.id.textBanner) TextView textBanner;

        public CourseViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }
}
