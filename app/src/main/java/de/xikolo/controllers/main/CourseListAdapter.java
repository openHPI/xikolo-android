package de.xikolo.controllers.main;

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
import java.util.List;
import java.util.Locale;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.models.Course;
import de.xikolo.models.base.SectionList;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.DisplayUtil;
import de.xikolo.utils.LanguageUtil;

public class CourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = CourseListAdapter.class.getSimpleName();

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private Course.Filter courseFilter;

    private SectionList<String, List<Course>> courseList;

    private OnCourseButtonClickListener callback;

    public CourseListAdapter(OnCourseButtonClickListener callback, Course.Filter courseFilter) {
        this.courseList = new SectionList<>();
        this.callback = callback;
        this.courseFilter = courseFilter;
    }

    public void update(SectionList<String, List<Course>> courseList) {
        this.courseList = courseList;
        notifyDataSetChanged();
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

            viewHolder.header.setText((String) courseList.getItem(position));
        } else {
            CourseViewHolder viewHolder = (CourseViewHolder) holder;

            final Course course = (Course) courseList.getItem(position);

            Context context = App.getInstance();

            DateFormat dateOut;
            if (DisplayUtil.is7inchTablet(context)) {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            } else {
                dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            }

            if (course.startDate != null && course.endDate != null) {
                viewHolder.textDate.setText(dateOut.format(course.startDate) + " - " + dateOut.format(course.endDate));
            } else if (course.startDate != null) {
                if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                    viewHolder.textDate.setText(context.getString(R.string.course_date_self_paced));
                } else if (DateUtil.nowIsAfter(course.startDate)) {
                    viewHolder.textDate.setText(String.format(context.getString(R.string.course_date_since), dateOut.format(course.startDate)));
                } else {
                    viewHolder.textDate.setText(String.format(context.getString(R.string.course_date_beginning), dateOut.format(course.startDate)));
                }
            } else {
                viewHolder.textDate.setText(context.getString(R.string.course_date_coming_soon));
            }

            viewHolder.textTitle.setText(course.title);
            viewHolder.textTeacher.setText(course.teachers);
            viewHolder.textLanguage.setText(LanguageUtil.languageForCode(context, course.language));

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
                } else if (DateUtil.nowIsAfter(course.endDate)) {
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

            ImageHelper.load(course.imageUrl, viewHolder.image);

            if (course.isEnrolled() && course.accessible) {
                viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnterButtonClicked(course.id);
                    }
                });
                viewHolder.buttonEnroll.setText(context.getString(R.string.btn_enter_course));
                viewHolder.buttonEnroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnterButtonClicked(course.id);
                    }
                });
            } else if (course.isEnrolled() && !course.accessible) {
                viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDetailButtonClicked(course.id);
                    }
                });
                viewHolder.buttonEnroll.setText(context.getString(R.string.btn_starts_soon));
                viewHolder.buttonEnroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDetailButtonClicked(course.id);
                    }
                });
            } else {
                viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onDetailButtonClicked(course.id);
                    }
                });
                viewHolder.buttonEnroll.setText(context.getString(R.string.btn_enroll_me));
                viewHolder.buttonEnroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onEnrollButtonClicked(course.id);
                    }
                });
            }
        }
    }

    public interface OnCourseButtonClickListener {

        void onEnrollButtonClicked(String courseId);

        void onEnterButtonClicked(String courseId);

        void onDetailButtonClicked(String courseId);

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
