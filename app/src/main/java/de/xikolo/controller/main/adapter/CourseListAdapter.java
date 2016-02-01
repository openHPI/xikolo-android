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
import de.xikolo.controller.helper.ImageLoaderController;
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
            HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);
            return headerViewHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_list, parent, false);
            CourseViewHolder courseViewHolder = new CourseViewHolder(view);
            return courseViewHolder;
        }
    }

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
                viewHolder.date.setText(dateOut.format(dateBegin) + " - " + dateOut.format(dateEnd));
            } else if (dateBegin != null) {
                viewHolder.date.setText(dateOut.format(dateBegin));
            } else {
                viewHolder.date.setText("");
            }

            viewHolder.title.setText(course.name);
            viewHolder.teacher.setText(course.lecturer);
            viewHolder.language.setText(LanguageUtil.languageForCode(context, course.language));

            if (courseFilter == CourseModel.CourseFilter.ALL) {
                viewHolder.description.setText(course.description);
                viewHolder.description.setVisibility(View.VISIBLE);

                if (DateUtil.nowIsBetween(course.available_from, course.available_to)) {
                    viewHolder.banner.setVisibility(View.VISIBLE);
                    viewHolder.banner.setText(context.getText(R.string.banner_running));
                    viewHolder.banner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_green));
                } else {
                    viewHolder.banner.setVisibility(View.GONE);
                }
            } else {
                viewHolder.description.setVisibility(View.GONE);
                if (DateUtil.nowIsBetween(course.available_from, course.available_to)) {
                    viewHolder.banner.setVisibility(View.VISIBLE);
                    viewHolder.banner.setText(context.getText(R.string.banner_running));
                    viewHolder.banner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_green));
                } else if (DateUtil.nowIsAfter(course.available_to)) {
                    viewHolder.banner.setVisibility(View.VISIBLE);
                    viewHolder.banner.setText(context.getText(R.string.banner_self_paced));
                    viewHolder.banner.setBackgroundColor(ContextCompat.getColor(context, R.color.banner_yellow));
                } else {
                    viewHolder.banner.setVisibility(View.GONE);
                }
            }

            ImageLoaderController.loadCourseImage(course.visual_url, viewHolder.img, viewHolder.container);

            if (course.is_enrolled && DateUtil.nowIsAfter(course.available_from)) {
                viewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnterButtonClicked(course);
                    }
                });
                viewHolder.enroll.setText(context.getString(R.string.btn_enter_course));
                viewHolder.enroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnterButtonClicked(course);
                    }
                });
            } else {
                viewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDetailButtonClicked(course);
                    }
                });
                viewHolder.enroll.setText(context.getString(R.string.btn_enroll_me));
                viewHolder.enroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnrollButtonClicked(course);
                    }
                });
            }

            if (course.is_enrolled && !DateUtil.nowIsAfter(course.available_from)) {
                viewHolder.enroll.setText(context.getString(R.string.btn_starts_soon));
                viewHolder.enroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDetailButtonClicked(course);
                    }
                });
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public interface OnCourseButtonClickListener {

        void onEnrollButtonClicked(Course course);

        void onEnterButtonClicked(Course course);

        void onDetailButtonClicked(Course course);

    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ViewGroup container;
        TextView title;
        TextView teacher;
        TextView date;
        TextView language;
        TextView description;
        ImageView img;
        Button enroll;
        TextView banner;

        public CourseViewHolder(View itemView) {
            super(itemView);
            container = (ViewGroup) itemView.findViewById(R.id.container);
            title = (TextView) itemView.findViewById(R.id.textTitle);
            teacher = (TextView) itemView.findViewById(R.id.textTeacher);
            date = (TextView) itemView.findViewById(R.id.textDate);
            language = (TextView) itemView.findViewById(R.id.textLanguage);
            description = (TextView) itemView.findViewById(R.id.textDescription);
            img = (ImageView) itemView.findViewById(R.id.imageView);
            enroll = (Button) itemView.findViewById(R.id.btnEnroll);
            banner = (TextView) itemView.findViewById(R.id.textBanner);
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
