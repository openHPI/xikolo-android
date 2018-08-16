package de.xikolo.controllers.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import de.xikolo.controllers.course.CourseActivityAutoBundle;
import de.xikolo.events.LoginEvent;
import de.xikolo.events.LogoutEvent;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.main.CertificateListPresenter;
import de.xikolo.presenters.main.CertificateListPresenterFactory;
import de.xikolo.presenters.main.CertificateListView;
import de.xikolo.views.SpaceItemDecoration;

public class CertificateListFragment extends MainFragment<CertificateListPresenter, CertificateListView> implements CertificateListView {

    public static final String TAG = CertificateListFragment.class.getSimpleName();

    @BindView(R.id.content_view) RecyclerView recyclerView;

    private CertificateListAdapter certificateListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_certificate_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        certificateListAdapter = new CertificateListAdapter(this, new CertificateListAdapter.OnCertificateCardClickListener() {
            @Override
            public void onViewCertificateClicked(String url) {
            }

            @Override
            public void onCourseClicked(String courseId) {
                Intent intent = CourseActivityAutoBundle.builder(courseId).build(App.getInstance());
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(App.getInstance()));
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
                    return 1;
                }

                @Override
                public int getItemCount() {
                    return certificateListAdapter.getItemCount();
                }
            }));
        recyclerView.setAdapter(certificateListAdapter);
    }

    @Override
    public void showCertificateList(List<Course> courses) {
        if (certificateListAdapter != null) {
            certificateListAdapter.update(courses);
        }
    }

    @Override
    public void showLoginRequiredMessage() {
        super.showLoginRequiredMessage();
        loadingStateHelper.setMessageOnClickListener(v -> activityCallback.selectDrawerSection(NavigationAdapter.NAV_PROFILE.getPosition()));
    }

    @Override
    public void showNoCertificatesMessage() {
        loadingStateHelper.setMessageTitle(R.string.notification_no_certificates);
        loadingStateHelper.setMessageSummary(R.string.notification_no_certificates_summary);
        loadingStateHelper.setMessageOnClickListener(v -> activityCallback.selectDrawerSection(NavigationAdapter.NAV_ALL_COURSES.getPosition()));
        loadingStateHelper.showMessage();
    }

    @Override
    public void onStart() {
        super.onStart();

        activityCallback.onFragmentAttached(NavigationAdapter.NAV_CERTIFICATES.getPosition(), getString(R.string.title_section_certificates));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
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
    protected PresenterFactory<CertificateListPresenter> getPresenterFactory() {
        return new CertificateListPresenterFactory();
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
