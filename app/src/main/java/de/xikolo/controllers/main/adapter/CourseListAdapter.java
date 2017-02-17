package de.xikolo.controllers.main.adapter;

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

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.helper.ImageController;
import de.xikolo.managers.CourseManager;
import de.xikolo.models.Course;
import de.xikolo.utils.Config;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.DisplayUtil;
import de.xikolo.utils.HeaderAndSectionsList;
import de.xikolo.utils.LanguageUtil;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = CourseListAdapter.class.getSimpleName();

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private CourseManager.CourseFilter courseFilter;

    private HeaderAndSectionsList<String, List<Course>> headerAndSectionsList;

    private OnCourseButtonClickListener callback;

    private CourseManager courseManager;

    private Realm realm;

    private RealmResults courseListPromise;

    public CourseListAdapter(OnCourseButtonClickListener callback, CourseManager.CourseFilter courseFilter) {
        this.headerAndSectionsList = new HeaderAndSectionsList<>();
        this.callback = callback;
        this.courseFilter = courseFilter;
        this.courseManager = new CourseManager(GlobalApplication.getInstance().getJobManager());
        this.realm = Realm.getDefaultInstance();

        init();
    }

    private void init() {
        courseListPromise = courseManager.listCoursesAsync(realm, new RealmChangeListener<RealmResults<Course>>() {
            @Override
            public void onChange(RealmResults<Course> element) {
                Context context = GlobalApplication.getInstance();
                headerAndSectionsList.clear();
                List<Course> subList;

                if (courseFilter == CourseManager.CourseFilter.ALL) {
                    subList = courseManager.listCurrentAndFutureCourses(realm, null);
                    if (subList.size() > 0) {
                        headerAndSectionsList.add(context.getString(R.string.header_current_courses),
                                subList);
                    }
                    subList = courseManager.listPastCourses(realm, null);
                    if (subList.size() > 0) {
                        headerAndSectionsList.add(context.getString(R.string.header_self_paced_courses),
                                subList);
                    }
                } else if (courseFilter == CourseManager.CourseFilter.MY) {
                    subList = courseManager.listCurrentAndPastCoursesWithEnrollment(realm, null);
                    if (subList.size() > 0) {
                        headerAndSectionsList.add(context.getString(R.string.header_my_current_courses),
                                subList);
                    }
                    subList = courseManager.listFutureCoursesWithEnrollment(realm, null);
                    if (subList.size() > 0) {
                        headerAndSectionsList.add(context.getString(R.string.header_my_future_courses),
                                subList);
                    }
                }
                CourseListAdapter.this.notifyDataSetChanged();
            }
        });
    }

    public void destroy() {
        if (courseListPromise != null) {
            courseListPromise.removeChangeListeners();
        }
    }

    public boolean isHeader(int position) {
        return headerAndSectionsList.isHeader(position);
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

            DateFormat dateOut;
            if (DisplayUtil.is7inchTablet(context)) {
                dateOut = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            } else {
                dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            }

            if (course.startDate != null && course.endDate != null) {
                viewHolder.textDate.setText(dateOut.format(course.startDate) + " - " + dateOut.format(course.endDate));
            } else if (course.startDate != null) {
                viewHolder.textDate.setText(dateOut.format(course.startDate));
            } else {
                viewHolder.textDate.setText("");
            }

            viewHolder.textTitle.setText(course.title);
            viewHolder.textTeacher.setText(course.teachers);
            viewHolder.textLanguage.setText(LanguageUtil.languageForCode(context, course.language));

            if (courseFilter == CourseManager.CourseFilter.ALL) {
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

            ImageController.load(Config.HTTPS + "://" + Config.HOST + course.imageUrl, viewHolder.image);

            if (course.isEnrolled() && DateUtil.nowIsAfter(course.startDate)) {
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

            if (course.isEnrolled() && !DateUtil.nowIsAfter(course.startDate)) {
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
