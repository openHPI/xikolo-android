package de.xikolo.controllers.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import androidx.annotation.NonNull;
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
import de.xikolo.controllers.helper.CourseListFilter;
import de.xikolo.models.Course;
import de.xikolo.models.CourseDate;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.SectionList;
import de.xikolo.utils.TimeUtil;

public class CourseListAdapter extends BaseCourseListAdapter {

    public static final String TAG = CourseListAdapter.class.getSimpleName();

    private OnDateOverviewClickListener onDateOverviewClickListener;
    private CourseListFilter courseFilter;

    private CourseDate nextDate;
    private int todaysDateCount = 0;
    private int nextSevenDaysDateCount = 0;
    private int futureDateCount = 0;

    public CourseListAdapter(Fragment fragment, CourseListFilter courseFilter, OnCourseButtonClickListener onCourseButtonClickListener, OnDateOverviewClickListener onDateOverviewClickListener) {
        this.fragment = fragment;
        this.courseList = new SectionList<>();
        this.courseFilter = courseFilter;
        this.onCourseButtonClickListener = onCourseButtonClickListener;
        this.onDateOverviewClickListener = onDateOverviewClickListener;
    }

    public void update(CourseDate nextDate, int todaysDateCount, int nextSevenDaysDateCount, int futureDateCount) {
        this.nextDate = nextDate;
        this.todaysDateCount = todaysDateCount;
        this.nextSevenDaysDateCount = nextSevenDaysDateCount;
        this.futureDateCount = futureDateCount;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (courseFilter == CourseListFilter.MY) {
            if (courseList.getItem(position) instanceof Course
                && ((Course) courseList.getItem(position)).id == null) // the course overview (Course with null id) with a header at position 0
                return ITEM_VIEW_TYPE_META;
            else if (courseList.isHeader(position))
                return ITEM_VIEW_TYPE_HEADER;
            else
                return ITEM_VIEW_TYPE_ITEM;
        } else {
            if (courseList.isHeader(position))
                return ITEM_VIEW_TYPE_HEADER;
            else
                return ITEM_VIEW_TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_META:
                return new CourseDatesViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.content_date_overview, parent, false)
                );
            case ITEM_VIEW_TYPE_HEADER:
                return new HeaderViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false)
                );
            default:
                return new CourseViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_list, parent, false)
                );
        }
    }

    @SuppressWarnings("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CourseDatesViewHolder) {
            CourseDatesViewHolder viewHolder = (CourseDatesViewHolder) holder;

            viewHolder.container.setOnClickListener(v -> onDateOverviewClickListener.onDateOverviewClicked());

            if (nextDate != null) {
                viewHolder.textNextDate.setText(
                    String.format(
                        App.getInstance().getString(R.string.course_date_next),
                        TimeUtil.getTimeLeftString(
                            nextDate.getDate().getTime() - new Date().getTime(),
                            App.getInstance()
                        )
                    )
                );
                viewHolder.textNextCourse.setText(
                    Course.get(nextDate.getCourseId()).title
                );

                viewHolder.titleOfNextDate.setText(nextDate.getTitle());

                viewHolder.nextDateContainer.setVisibility(View.VISIBLE);
            } else {
                viewHolder.nextDateContainer.setVisibility(View.GONE);
            }

            viewHolder.numberOfDatesToday.setText(String.valueOf(todaysDateCount));
            viewHolder.numberOfDatesWeek.setText(String.valueOf(nextSevenDaysDateCount));
            viewHolder.numberOfAllDates.setText(String.valueOf(futureDateCount));
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

            if (course.teachers == null || "".equals(course.teachers)) {
                viewHolder.textTeacher.setVisibility(View.GONE);
            } else {
                viewHolder.textTeacher.setVisibility(View.VISIBLE);
            }

            if (courseFilter == CourseListFilter.ALL) {
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
            viewHolder.buttonCourseDetails.setOnClickListener(v -> onCourseButtonClickListener.onDetailButtonClicked(course.id));

            if (course.isEnrolled() && course.accessible) {
                viewHolder.layout.setOnClickListener(v -> onCourseButtonClickListener.onContinueButtonClicked(course.id));

                viewHolder.buttonCourseAction.setText(context.getString(R.string.btn_continue_course));
                viewHolder.buttonCourseAction.setOnClickListener(v -> onCourseButtonClickListener.onContinueButtonClicked(course.id));

                viewHolder.buttonCourseDetails.setVisibility(View.GONE);

            } else if (course.isEnrolled() && !course.accessible) {
                viewHolder.layout.setOnClickListener(v -> onCourseButtonClickListener.onDetailButtonClicked(course.id));

                viewHolder.buttonCourseAction.setText(context.getString(R.string.btn_starts_soon));
                viewHolder.buttonCourseAction.setEnabled(false);
                viewHolder.buttonCourseAction.setClickable(false);

            } else {
                viewHolder.layout.setOnClickListener(v -> onCourseButtonClickListener.onDetailButtonClicked(course.id));

                viewHolder.buttonCourseAction.setText(context.getString(R.string.btn_enroll));
                viewHolder.buttonCourseAction.setOnClickListener(v -> onCourseButtonClickListener.onEnrollButtonClicked(course.id));
            }
        }
    }

    public interface OnDateOverviewClickListener {

        void onDateOverviewClicked();
    }

    static class CourseDatesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container)
        View container;

        @BindView(R.id.nextDateContainer)
        ViewGroup nextDateContainer;

        @BindView(R.id.textNextDate)
        TextView textNextDate;

        @BindView(R.id.textNextCourse)
        TextView textNextCourse;

        @BindView(R.id.textTitleOfNextDate)
        TextView titleOfNextDate;

        @BindView(R.id.textNumberOfDatesToday)
        TextView numberOfDatesToday;

        @BindView(R.id.textNumberOfDatesWeek)
        TextView numberOfDatesWeek;

        @BindView(R.id.textNumberOfAllDates)
        TextView numberOfAllDates;

        CourseDatesViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
