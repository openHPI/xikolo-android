package de.xikolo.controllers.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.controllers.channels.ChannelDetailsActivityAutoBundle;
import de.xikolo.controllers.course.CourseActivityAutoBundle;
import de.xikolo.events.LoginEvent;
import de.xikolo.events.LogoutEvent;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.main.ChannelListPresenter;
import de.xikolo.presenters.main.ChannelListPresenterFactory;
import de.xikolo.presenters.main.ChannelListView;
import de.xikolo.views.AutofitRecyclerView;
import de.xikolo.views.SpaceItemDecoration;

public class ChannelListFragment extends PresenterMainFragment<ChannelListPresenter, ChannelListView> implements ChannelListView {

    public static final String TAG = ChannelListFragment.class.getSimpleName();

    @BindView(R.id.content_view) AutofitRecyclerView recyclerView;

    private ChannelListAdapter channelListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_channel_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        channelListAdapter = new ChannelListAdapter(new ChannelListAdapter.OnChannelCardClickListener() {
            @Override
            public void onChannelClicked(String channelId) {
                showChannel(channelId);
            }

            @Override
            public void onCourseClicked(Course course) {
                showCourse(course);
            }

            @Override
            public void onMoreCoursesClicked(String channelId, int scrollPosition) {
                showChannelCourses(channelId, scrollPosition);
            }
        });

        recyclerView.setAdapter(channelListAdapter);

        recyclerView.addItemDecoration(new SpaceItemDecoration(
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
                        return recyclerView.getSpanCount();
                    }

                    @Override
                    public int getItemCount() {
                        return channelListAdapter.getItemCount();
                    }
                }));
    }

    @Override
    public void showChannelList(List<Channel> channelList) {
        if (channelListAdapter != null) {
            channelListAdapter.update(channelList);
        }
    }

    @Override
    public void showCourse(Course course) {
        Intent intent = CourseActivityAutoBundle.builder().courseId(course.id).build(App.getInstance());
        startActivity(intent);
    }

    @Override
    public void showChannelCourses(String channelId, int scrollPosition) {
        Intent intent = ChannelDetailsActivityAutoBundle.builder(channelId).scrollToCoursePosition(scrollPosition).build(App.getInstance());
        startActivity(intent);
    }

    @Override
    public void showChannel(String channelId) {
        Intent intent = ChannelDetailsActivityAutoBundle.builder(channelId).build(App.getInstance());
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getActivityCallback() != null) {
            getActivityCallback().onFragmentAttached(NavigationAdapter.NAV_CHANNELS.getPosition(), getString(R.string.title_section_channels));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivityCallback() != null && !getActivityCallback().isDrawerOpen()) {
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
    protected PresenterFactory<ChannelListPresenter> getPresenterFactory() {
        return new ChannelListPresenterFactory();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        if (presenter != null) {
            presenter.onRefresh();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogoutEvent(LogoutEvent event) {
        if (presenter != null) {
            presenter.onRefresh();
        }
    }
}
