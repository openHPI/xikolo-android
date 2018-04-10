package de.xikolo.controllers.channels;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import java.util.List;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.controllers.course.CourseActivityAutoBundle;
import de.xikolo.controllers.course.CourseDetailsActivityAutoBundle;
import de.xikolo.controllers.login.LoginActivityAutoBundle;
import de.xikolo.controllers.main.CourseListAdapter;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.channels.ChannelDetailsPresenter;
import de.xikolo.presenters.channels.ChannelDetailsPresenterFactory;
import de.xikolo.presenters.channels.ChannelDetailsView;
import de.xikolo.utils.MarkdownUtil;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;

public class ChannelDetailsFragment extends LoadingStatePresenterFragment<ChannelDetailsPresenter, ChannelDetailsView> implements ChannelDetailsView {

    public static final String TAG = ChannelDetailsFragment.class.getSimpleName();

    @AutoBundleField String channelId;

    @AutoBundleField (required = false) boolean scrollToCourses = false;

    @BindView(R.id.content_view) NestedScrollView scrollView;
    @BindView(R.id.layout_header) FrameLayout layoutHeader;
    @BindView(R.id.image_channel) ImageView imageChannel;
    @BindView(R.id.text_title) TextView textTitle;
    @BindView(R.id.text_description) TextView textDescription;
    @BindView(R.id.course_list) AutofitRecyclerView courseList;

    private ChannelCourseListAdapter courseListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseListAdapter = new ChannelCourseListAdapter(this, new CourseListAdapter.OnCourseButtonClickListener() {
            @Override
            public void onEnrollButtonClicked(String courseId) {
                presenter.onEnrollButtonClicked(courseId);
            }

            @Override
            public void onContinueButtonClicked(String courseId) {
                presenter.onCourseEnterButtonClicked(courseId);
            }

            @Override
            public void onDetailButtonClicked(String courseId) {
                presenter.onCourseDetailButtonClicked(courseId);
            }
        });

        courseList.setAdapter(courseListAdapter);

        courseList.addItemDecoration(new SpaceItemDecoration(
                getActivity().getResources().getDimensionPixelSize(R.dimen.card_horizontal_margin),
                getActivity().getResources().getDimensionPixelSize(R.dimen.card_vertical_margin),
                false,
                new SpaceItemDecoration.RecyclerViewInfo() {
                    @Override
                    public boolean isHeader(int position) {
                        return false;
                    }

                    @Override
                    public int getSpanCount() {
                        return courseList.getSpanCount();
                    }

                    @Override
                    public int getItemCount() {
                        return courseListAdapter.getItemCount();
                    }
                }));

        courseList.setNestedScrollingEnabled(false);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_channel_details;
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
    public void setupView(Channel channel) {
        if (getActivity() instanceof ChannelDetailsActivity) {
            layoutHeader.setVisibility(View.GONE);
        }
        else {
            GlideApp.with(this).load(channel.imageUrl).into(imageChannel);
            textTitle.setText(channel.name);
        }

        MarkdownUtil.formatAndSet(channel.description, textDescription);

        if(courseListAdapter != null)
            courseListAdapter.setButtonColor(Color.parseColor(channel.color));

        if(scrollToCourses)
            scrollView.smoothScrollTo(0, courseList.getTop());
    }

    @Override
    public void showCourses(List<Course> courses) {
        if(courseListAdapter != null) {
            courseListAdapter.update(courses);
            courseListAdapter.notifyDataSetChanged();
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
    public void openLogin() {
        Intent intent = LoginActivityAutoBundle.builder().build(getActivity());
        startActivity(intent);
    }

    @NonNull
    @Override
    protected PresenterFactory<ChannelDetailsPresenter> getPresenterFactory() {
        return new ChannelDetailsPresenterFactory(channelId);
    }

}
