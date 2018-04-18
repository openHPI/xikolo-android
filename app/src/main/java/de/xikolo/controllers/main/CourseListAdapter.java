package de.xikolo.controllers.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BaseCourseListAdapter;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.utils.DateUtil;

public class CourseListAdapter extends BaseCourseListAdapter {

    public static final String TAG = CourseListAdapter.class.getSimpleName();

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private Course.Filter courseFilter;

    public CourseListAdapter(Fragment fragment, OnCourseButtonClickListener callback, Course.Filter courseFilter) {
        this.fragment = fragment;
        this.courseList = new SectionList<>();
        this.callback = callback;
        this.courseFilter = courseFilter;
    }

    @Override
    public int getItemViewType(int position) {
        if (courseList.isHeader(position)) {
            return ITEM_VIEW_TYPE_HEADER;
        } else {
            return ITEM_VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_list, parent, false);
            return new CourseViewHolder(view);
        }
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
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

            if (course.teachers == null || "".equals(course.teachers)) {
                viewHolder.textTeacher.setVisibility(View.GONE);
            } else {
                viewHolder.textTeacher.setVisibility(View.VISIBLE);
            }

            if (courseFilter == Course.Filter.ALL) {
                viewHolder.textDescription.setText(course.shortAbstract);
                viewHolder.textDescription.setVisibility(View.VISIBLE);

                if (DateUtil.nowIsBetween(course.startDate, course.endDate)) {
                    viewHolder.textBanner.setVisibility(View.VISIBLE);
                    viewHolder.textBanner.setText(context.getText(R.string.banner_running));
                    viewHolder.textBanner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_green));
                } else {
                    viewHolder.textBanner.setVisibility(View.GONE);
                }
            } else {
                viewHolder.textDescription.setVisibility(View.GONE);
                if (DateUtil.nowIsBetween(course.startDate, course.endDate)) {
                    viewHolder.textBanner.setVisibility(View.VISIBLE);
                    viewHolder.textBanner.setText(context.getText(R.string.banner_running));
                    viewHolder.textBanner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_green));
                } else if (DateUtil.isPast(course.endDate)) {
                    viewHolder.textBanner.setVisibility(View.VISIBLE);
                    viewHolder.textBanner.setText(context.getText(R.string.banner_self_paced));
                    viewHolder.textBanner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_yellow));
                } else {
                    viewHolder.textBanner.setVisibility(View.GONE);
                }
            }

            if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                viewHolder.textBanner.setVisibility(View.GONE);
            }

            GlideApp.with(fragment).load(course.imageUrl).into(viewHolder.image);

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

}
