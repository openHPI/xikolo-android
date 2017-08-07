package de.xikolo.controllers.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.announcement.AnnouncementActivityAutoBundle;
import de.xikolo.models.Announcement;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.main.NewsListPresenter;
import de.xikolo.presenters.main.NewsListPresenterFactory;
import de.xikolo.presenters.main.NewsListView;

public class NewsListFragment extends MainFragment<NewsListPresenter, NewsListView> implements NewsListView {

    public static final String TAG = NewsListFragment.class.getSimpleName();

    @BindView(R.id.content_view) RecyclerView recyclerView;

    private NewsListAdapter newsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_news_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newsListAdapter = new NewsListAdapter(new NewsListAdapter.OnAnnouncementClickListener() {
            @Override
            public void onAnnouncementClicked(String announcementId) {
                presenter.onAnnouncementClicked(announcementId);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(newsListAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        activityCallback.onFragmentAttached(NavigationAdapter.NAV_NEWS.getPosition(), getString(R.string.title_section_news));
    }

    @Override
    public void openAnnouncement(String announcementId) {
        Intent intent = AnnouncementActivityAutoBundle.builder(announcementId).build(getActivity());
        startActivity(intent);
    }

    @Override
    public void showAnnouncementList(List<Announcement> announcementList) {
        if (newsListAdapter != null) {
            newsListAdapter.update(announcementList);
        }
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
    protected PresenterFactory<NewsListPresenter> getPresenterFactory() {
        return new NewsListPresenterFactory();
    }

}
