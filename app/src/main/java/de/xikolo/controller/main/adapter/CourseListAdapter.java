package de.xikolo.controller.main.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.helper.ImageController;
import de.xikolo.data.entities.Course;
import de.xikolo.model.CourseModel;
import de.xikolo.util.DateUtil;
import de.xikolo.util.DisplayUtil;
import de.xikolo.util.HeaderAndSectionsList;
import de.xikolo.util.LanguageUtil;

public class CourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = CourseListAdapter.class.getSimpleName();

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private CourseModel.CourseFilter courseFilter;

    private HeaderAndSectionsList<String, List<Course>> headerAndSectionsList;

    private OnCourseButtonClickListener callback;

    public CourseListAdapter(OnCourseButtonClickListener callback, CourseModel.CourseFilter courseFilter) {
        this.headerAndSectionsList = new HeaderAndSectionsList<>();
        this.callback = callback;
        this.courseFilter = courseFilter;
    }

    public boolean isHeader(int position) {
        return headerAndSectionsList.isHeader(position);
    }

    public void updateCourses(List<Course> courses) {
        if (courses == null) throw new NullPointerException("Courses can't be null");

        Context context = GlobalApplication.getInstance();

        headerAndSectionsList.clear();
        List<Course> subList;
        if (courses.size() > 0) {
            if (courseFilter == CourseModel.CourseFilter.ALL) {
                subList = CourseModel.getCurrentAndFutureCourses(courses);
                if (subList.size() > 0) {
                    headerAndSectionsList.add(context.getString(R.string.header_current_courses),
                            subList);
                }
                subList = CourseModel.getPastCourses(courses);
                if (subList.size() > 0) {
                    headerAndSectionsList.add(context.getString(R.string.header_self_paced_courses),
                            subList);
                }
            } else if (courseFilter == CourseModel.CourseFilter.MY) {
                subList = CourseModel.getCurrentAndPastCourses(courses);
                if (subList.size() > 0) {
                    headerAndSectionsList.add(context.getString(R.string.header_my_current_courses),
                            subList);
                }
                subList = CourseModel.getFutureCourses(courses);
                if (subList.size() > 0) {
                    headerAndSectionsList.add(context.getString(R.string.header_my_future_courses),
                            subList);
                }
            }
        }

        this.notifyDataSetChanged();
    }

    public void clear() {
        headerAndSectionsList.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return headerAndSectionsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (headerAndSectionsList.isHeader(position)) {
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

            viewHolder.header.setText((String) headerAndSectionsList.getItem(position));
        } else {
            CourseViewHolder viewHolder = (CourseViewHolder) holder;

            final Course course = (Course) headerAndSectionsList.getItem(position);

            Context context = GlobalApplication.getInstance();

            Date dateBegin = DateUtil.parse(course.available_from);
            Date dateEnd = DateUtil.parse(course.available_to);

            DateFormat dateOut;
            if (DisplayUtil.is7inchTablet(context)) {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            } else {
                dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            }

            if (dateBegin != null && dateEnd != null) {
                viewHolder.textDate.setText(dateOut.format(dateBegin) + " - " + dateOut.format(dateEnd));
            } else if (dateBegin != null) {
                viewHolder.textDate.setText(dateOut.format(dateBegin));
            } else {
                viewHolder.textDate.setText("");
            }

            viewHolder.textTitle.setText(course.name);
            viewHolder.textTeacher.setText(course.lecturer);
            viewHolder.textLanguage.setText(LanguageUtil.languageForCode(context, course.language));

            if (courseFilter == CourseModel.CourseFilter.ALL) {
                viewHolder.textDescription.setText(course.description);
                viewHolder.textDescription.setVisibility(View.VISIBLE);

                if (DateUtil.nowIsBetween(course.available_from, course.available_to)) {
                    viewHolder.textBanner.setVisibility(View.VISIBLE);
                    viewHolder.textBanner.setText(context.getText(R.string.banner_running));
                    viewHolder.textBanner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_green));
                } else {
                    viewHolder.textBanner.setVisibility(View.GONE);
                }
            } else {
                viewHolder.textDescription.setVisibility(View.GONE);
                if (DateUtil.nowIsBetween(course.available_from, course.available_to)) {
                    viewHolder.textBanner.setVisibility(View.VISIBLE);
                    viewHolder.textBanner.setText(context.getText(R.string.banner_running));
                    viewHolder.textBanner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_green));
                } else if (DateUtil.nowIsAfter(course.available_to)) {
                    viewHolder.textBanner.setVisibility(View.VISIBLE);
                    viewHolder.textBanner.setText(context.getText(R.string.banner_self_paced));
                    viewHolder.textBanner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_yellow));
                } else {
                    viewHolder.textBanner.setVisibility(View.GONE);
                }
            }

            ImageController.load(course.visual_url, viewHolder.image);

            if (course.is_enrolled && DateUtil.nowIsAfter(course.available_from)) {
                viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnterButtonClicked(course);
                    }
                });
                viewHolder.buttonEnroll.setText(context.getString(R.string.btn_enter_course));
                viewHolder.buttonEnroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnterButtonClicked(course);
                    }
                });
            } else {
                viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDetailButtonClicked(course);
                    }
                });
                viewHolder.buttonEnroll.setText(context.getString(R.string.btn_enroll_me));
                viewHolder.buttonEnroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnrollButtonClicked(course);
                    }
                });
            }

            if (course.is_enrolled && !DateUtil.nowIsAfter(course.available_from)) {
                viewHolder.buttonEnroll.setText(context.getString(R.string.btn_starts_soon));
                viewHolder.buttonEnroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDetailButtonClicked(course);
                    }
                });
            }
        }
    }

    public interface OnCourseButtonClickListener {

        void onEnrollButtonClicked(Course course);

        void onEnterButtonClicked(Course course);

        void onDetailButtonClicked(Course course);

    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        ViewGroup layout;
        TextView textTitle;
        TextView textTeacher;
        TextView textDate;
        TextView textLanguage;
        TextView textDescription;
        ImageView image;
        Button buttonEnroll;
        TextView textBanner;

        public CourseViewHolder(View itemView) {
            super(itemView);
            layout = (ViewGroup) itemView.findViewById(R.id.container);
            textTitle = (TextView) itemView.findViewById(R.id.textTitle);
            textTeacher = (TextView) itemView.findViewById(R.id.textTeacher);
            textDate = (TextView) itemView.findViewById(R.id.textDate);
            textLanguage = (TextView) itemView.findViewById(R.id.textLanguage);
            textDescription = (TextView) itemView.findViewById(R.id.textDescription);
            image = (ImageView) itemView.findViewById(R.id.imageView);
            buttonEnroll = (Button) itemView.findViewById(R.id.btnEnroll);
            textBanner = (TextView) itemView.findViewById(R.id.textBanner);
        }

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ViewGroup container;
        TextView header;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            container = (ViewGroup) itemView.findViewById(R.id.container);
            header = (TextView) itemView.findViewById(R.id.textHeader);
        }

    }

}
