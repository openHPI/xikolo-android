package de.xikolo.presenters.main;

import java.util.List;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.models.Course;

public class CourseListFilterChannelPresenter extends CourseListPresenter {

    private String channelId;

    public CourseListFilterChannelPresenter(String channelId){
        this.channelId = channelId;
    }

    @Override
    public void updateContent() {
        courseList.clear();
        List<Course> subList;

        if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
            subList = courseManager.listFutureCoursesForChannel(realm, channelId);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_future_courses),
                        subList
                );
            }
            subList = courseManager.listCurrentAndPastCoursesForChannel(realm, channelId);
            if (subList.size() > 0) {
                courseList.add(App.getInstance().getString(
                        R.string.header_self_paced_courses),
                        subList
                );
            }
        } else {
            subList = courseManager.listCurrentAndFutureCoursesForChannel(realm, channelId);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_current_and_upcoming_courses),
                        subList
                );
            }
            subList = courseManager.listPastCoursesForChannel(realm, channelId);
            if (subList.size() > 0) {
                courseList.add(
                        App.getInstance().getString(R.string.header_self_paced_courses),
                        subList
                );
            }
        }

        getViewOrThrow().showCourseList(courseList);
    }

    @Override
    public void requestCourses(boolean userRequest) {
        super.requestCourses(userRequest);
    }

    @Override
    protected void setCourseListPromise() {
        this.courseListPromise = courseManager.listCoursesForChannel(channelId, realm, getCourseListChangeListener());
    }

}
