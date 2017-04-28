package de.xikolo.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yatatsu.autobundle.AutoBundleField;

import java.util.List;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.activities.CourseActivityAutoBundle;
import de.xikolo.controllers.activities.CourseDetailsActivityAutoBundle;
import de.xikolo.controllers.adapters.CourseListAdapter;
import de.xikolo.controllers.navigation.NavigationAdapter;
import de.xikolo.models.Course;
import de.xikolo.presenters.CourseListFilterAllPresenterFactory;
import de.xikolo.presenters.CourseListFilterMyPresenterFactory;
import de.xikolo.presenters.CourseListPresenter;
import de.xikolo.presenters.CourseListView;
import de.xikolo.presenters.PresenterFactory;
import de.xikolo.utils.HeaderAndSectionsList;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;

public class CourseListFragment extends MainFragment<CourseListPresenter, CourseListView> implements CourseListView {

    public static final String TAG = CourseListFragment.class.getSimpleName();

    @AutoBundleField Course.Filter filter;

    @BindView(R.id.recyclerView) AutofitRecyclerView recyclerView;

    private CourseListAdapter courseListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        presenter.onCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseListAdapter = new CourseListAdapter(new CourseListAdapter.OnCourseButtonClickListener() {
            @Override
            public void onEnrollButtonClicked(String courseId) {
                presenter.onEnrollButtonClicked(courseId);
            }

            @Override
            public void onEnterButtonClicked(String courseId) {
                presenter.onCourseEnterButtonClicked(courseId);
            }

            @Override
            public void onDetailButtonClicked(String courseId) {
                presenter.onCourseDetailButtonClicked(courseId);
            }
        }, filter);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(courseListAdapter);

        recyclerView.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return courseListAdapter.isHeader(position) ? recyclerView.getSpanCount() : 1;
            }
        });

        recyclerView.addItemDecoration(new SpaceItemDecoration(
                getActivity().getResources().getDimensionPixelSize(R.dimen.card_horizontal_margin),
                getActivity().getResources().getDimensionPixelSize(R.dimen.card_vertical_margin),
                false,
                new SpaceItemDecoration.RecyclerViewInfo() {
                    @Override
                    public boolean isHeader(int position) {
                        return courseListAdapter.isHeader(position);
                    }

                    @Override
                    public int getSpanCount() {
                        return recyclerView.getSpanCount();
                    }

                    @Override
                    public int getItemCount() {
                        return courseListAdapter.getItemCount();
                    }
                }));
    }

    @Override
    public void onStart() {
        super.onStart();

        if (filter == Course.Filter.ALL) {
            activityCallback.onFragmentAttached(NavigationAdapter.NAV_ALL_COURSES.getPosition(), getString(R.string.title_section_all_courses));
        } else {
            activityCallback.onFragmentAttached(NavigationAdapter.NAV_MY_COURSES.getPosition(), getString(R.string.title_section_my_courses));
        }
    }

    @Override
    public void showCourseList(HeaderAndSectionsList<String, List<Course>> courseList) {
        if (courseListAdapter != null) {
            courseListAdapter.update(courseList);
        }
    }

    @Override
    public void enterCourse(String courseId) {
        Intent intent = CourseActivityAutoBundle.builder().courseId(courseId).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void enterCourseDetails(String courseId) {
        Intent intent = CourseDetailsActivityAutoBundle.builder(courseId).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (activityCallback != null && !activityCallback.isDrawerOpen()) {
            inflater.inflate(R.menu.refresh, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseListPresenter> getPresenterFactory() {
        return filter == Course.Filter.ALL ? new CourseListFilterAllPresenterFactory() : new CourseListFilterMyPresenterFactory();
    }

    @Override
    public void showLoginRequiredMessage() {
        super.showLoginRequiredMessage();
        loadingStateController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition());
            }
        });
    }

    @Override
    public void showNoEnrollmentsMessage() {
        loadingStateController.setTitle(R.string.notification_no_enrollments);
        loadingStateController.setSummary(R.string.notification_no_enrollments_summary);
        loadingStateController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCallback.selectDrawerSection(NavigationAdapter.NAV_ALL_COURSES.getPosition());
            }
        });
        loadingStateController.showMessage();
    }

}
