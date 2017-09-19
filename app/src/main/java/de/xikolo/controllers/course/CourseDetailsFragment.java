package de.xikolo.controllers.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.CourseDetailsPresenter;
import de.xikolo.presenters.course.CourseDetailsPresenterFactory;
import de.xikolo.presenters.course.CourseDetailsView;
import de.xikolo.utils.ToastUtil;

public class CourseDetailsFragment extends LoadingStatePresenterFragment<CourseDetailsPresenter, CourseDetailsView> implements CourseDetailsView {

    public static final String TAG = CourseDetailsFragment.class.getSimpleName();

    @AutoBundleField String courseId;

    @BindView(R.id.image) ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_course_details;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int videoId = item.getItemId();
        switch (videoId) {
            case R.id.action_refresh:
                presenter.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setupView(Course course) {
        if (getActivity() instanceof CourseDetailsActivity) {
            imageView.setVisibility(View.GONE);
        } else {
            ImageHelper.load(course.imageUrl, imageView);
        }
    }

    @Override
    public void showErrorToast() {
        ToastUtil.show(R.string.error);
    }

    @Override
    public void showNoNetworkToast() {
        ToastUtil.show(R.string.toast_no_network);
    }

    @Override
    public void finishActivity() {
        getActivity().finish();
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseDetailsPresenter> getPresenterFactory() {
        return new CourseDetailsPresenterFactory(courseId);
    }

}
