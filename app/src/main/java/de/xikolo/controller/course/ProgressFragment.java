package de.xikolo.controller.course;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.BaseFragment;
import de.xikolo.controller.course.adapter.ProgressListAdapter;
import de.xikolo.controller.helper.NotificationController;
import de.xikolo.controller.helper.RefeshLayoutController;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Module;
import de.xikolo.model.ModuleModel;
import de.xikolo.model.Result;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;
import de.xikolo.view.SpaceItemDecoration;

public class ProgressFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = ProgressFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private static final String KEY_MODULES = "key_modules";

    private Course mCourse;
    private List<Module> mModules;

    private ModuleModel mModuleModel;

    private ProgressListAdapter mAdapter;

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;

    private NotificationController mNotificationController;

    public ProgressFragment() {
        // Required empty public constructor
    }

    public static ProgressFragment newInstance(Course course) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COURSE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mModules != null) {
            outState.putParcelableArrayList(KEY_MODULES, (ArrayList<Module>) mModules);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = getArguments().getParcelable(ARG_COURSE);
        }
        if (savedInstanceState != null) {
            mModules = savedInstanceState.getParcelableArrayList(KEY_MODULES);
        }
        setHasOptionsMenu(true);

        mModuleModel = new ModuleModel(jobManager);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_progress, container, false);

        mRecyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);

        mAdapter = new ProgressListAdapter(getActivity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SpaceItemDecoration(
                0,
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
                        return mAdapter.getItemCount();
                    }
                }
        ));

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        RefeshLayoutController.setup(mRefreshLayout, this);

        mNotificationController = new NotificationController(layout);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mModules == null) {
            mNotificationController.setProgressVisible(true);
            requestProgress(false);
        } else {
            mAdapter.updateModules(mModules);
        }
    }

    private void requestProgress(final boolean userRequest) {
        Result<List<Module>> result = new Result<List<Module>>() {
            @Override
            protected void onSuccess(List<Module> result, DataSource dataSource) {
                if (result.size() > 0) {
                    mNotificationController.setInvisible();
                }
                if (!NetworkUtil.isOnline(getActivity()) && dataSource.equals(DataSource.LOCAL) ||
                        dataSource.equals(DataSource.NETWORK)) {
                    mRefreshLayout.setRefreshing(false);
                }

                mModules = result;

                if (!NetworkUtil.isOnline(getActivity()) && dataSource.equals(DataSource.LOCAL) && result.size() == 0) {
                    mAdapter.clear();
                    mRefreshLayout.setRefreshing(false);
                    mNotificationController.setTitle(R.string.notification_no_network);
                    mNotificationController.setSummary(R.string.notification_no_network_with_offline_mode_summary);
                    mNotificationController.setNotificationVisible(true);
                } else if (mModules != null && mModules.size() > 0) {
                    mAdapter.updateModules(mModules);
                }
            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                if (warnCode == WarnCode.NO_NETWORK && userRequest) {
                    NetworkUtil.showNoConnectionToast();
                }
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                mNotificationController.setInvisible();
                mRefreshLayout.setRefreshing(false);
                ToastUtil.show(R.string.error);
            }
        };

        if (!mNotificationController.isProgressVisible()) {
            mRefreshLayout.setRefreshing(true);
        }

        mModuleModel.getModules(result, mCourse, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
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

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        requestProgress(true);
    }

}
