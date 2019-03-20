package de.xikolo.controllers.channels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BaseCourseListAdapter;
import de.xikolo.controllers.main.CourseListAdapter;
import de.xikolo.models.Course;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.MarkdownUtil;
import de.xikolo.utils.SectionList;

public class ChannelCourseListAdapter extends BaseCourseListAdapter {

    public static final String TAG = ChannelCourseListAdapter.class.getSimpleName();

    public static final int ITEM_VIEW_TYPE_META = 0;
    public static final int ITEM_VIEW_TYPE_HEADER = 1;
    public static final int ITEM_VIEW_TYPE_ITEM = 2;

    private int channelColor = -1;

    ChannelCourseListAdapter(Fragment fragment, CourseListAdapter.OnCourseButtonClickListener callback) {
        this.fragment = fragment;
        this.courseList = new SectionList<>();
        this.callback = callback;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return ITEM_VIEW_TYPE_META;
        else if (courseList.isHeader(position))
            return ITEM_VIEW_TYPE_HEADER;
        else
            return ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_META:
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_description, parent, false);
                return new ChannelCourseListAdapter.MetaViewHolder(view1);
            case ITEM_VIEW_TYPE_HEADER:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
                return new ChannelCourseListAdapter.HeaderViewHolder(view2);
            default:
                View view3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_list, parent, false);
                return new ChannelCourseListAdapter.CourseViewHolder(view3);
        }
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MetaViewHolder) {
            MetaViewHolder viewHolder = (MetaViewHolder) holder;
            if (courseList.getItem(position) != null)
                MarkdownUtil.formatAndSet((String) courseList.getItem(position), viewHolder.text);
            else
                viewHolder.text.setVisibility(View.GONE);
        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;

            String header = (String) courseList.getItem(position);
            if (header == null) {
                viewHolder.header.setVisibility(View.GONE);
            } else {
                viewHolder.header.setText(header);
                viewHolder.header.setVisibility(View.VISIBLE);
            }
        } else {
            CourseViewHolder viewHolder = (CourseViewHolder) holder;

            final Course course = (Course) courseList.getItem(position);

            Context context = App.getInstance();

            viewHolder.textDate.setText(course.getFormattedDate());
            viewHolder.textTitle.setText(course.title);
            viewHolder.textTeacher.setText(course.teachers);
            viewHolder.textLanguage.setText(course.getFormattedLanguage());

            if (course.teachers == null || course.teachers.equals("")) {
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

            if (channelColor != -1) {
                viewHolder.buttonCourseAction.setTextColor(channelColor);
                viewHolder.buttonCourseDetails.setTextColor(channelColor);
            }

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
    }

    public void setButtonColor(@ColorInt int color) {
        this.channelColor = color;
    }

    static class MetaViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text) TextView text;

        public MetaViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
