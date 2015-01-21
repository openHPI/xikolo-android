package de.xikolo.controller.course;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.BaseFragment;
import de.xikolo.controller.ModuleActivity;
import de.xikolo.controller.course.adapter.ItemListAdapter;
import de.xikolo.controller.course.adapter.ModuleListAdapter;
import de.xikolo.controller.helper.RefeshLayoutController;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.model.ItemModel;
import de.xikolo.model.ModuleModel;
import de.xikolo.model.Result;
import de.xikolo.util.NetworkUtil;
import de.xikolo.util.ToastUtil;

public class CourseLearningsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        ModuleListAdapter.OnModuleButtonClickListener, ItemListAdapter.OnItemButtonClickListener {

    public final static String TAG = CourseLearningsFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private static final String KEY_MODULES = "key_modules";

    private static final int REQUEST_CODE_MODULES = 1;

    private AbsListView mListView;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mProgress;

    private ModuleModel mModuleModel;
    private ItemModel mItemModel;
    private Result<List<Module>> mModuleResult;
    private ModuleListAdapter mAdapter;

    private Course mCourse;
    private List<Module> mModules;

    public CourseLearningsFragment() {
        // Required empty public constructor
    }

    public static CourseLearningsFragment newInstance(Course course) {
        CourseLearningsFragment fragment = new CourseLearningsFragment();
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

        mModuleModel = new ModuleModel(getActivity(), jobManager, databaseHelper);
        mItemModel = new ItemModel(getActivity(), jobManager, databaseHelper);
        mModuleResult = new Result<List<Module>>() {
            @Override
            protected void onSuccess(List<Module> result, DataSource dataSource) {
                mRefreshLayout.setRefreshing(false);
                mProgress.setVisibility(View.GONE);

                mAdapter.updateModules(result);
                mModules = result;

                for (final Module module : mModules) {
                    if (module.items == null || module.items.size() == 0) {

                        Result<List<Item>> itemResult = new Result<List<Item>>() {
                            @Override
                            protected void onSuccess(List<Item> result, DataSource dataSource) {
                                module.items = result;
                                mAdapter.updateModules(mModules);
                            }
                        };
                        mItemModel.getItems(itemResult, mCourse, module);
                    }
                }
            }

            @Override
            protected void onWarning(WarnCode warnCode) {
                if (warnCode == WarnCode.NO_NETWORK) {
                    NetworkUtil.showNoConnectionToast(getActivity());
                }
                mRefreshLayout.setRefreshing(false);
                mProgress.setVisibility(View.GONE);
            }

            @Override
            protected void onError(ErrorCode errorCode) {
                ToastUtil.show(getActivity(), R.string.error);
                mRefreshLayout.setRefreshing(false);
                mProgress.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_learnings, container, false);

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        RefeshLayoutController.setup(mRefreshLayout, this);

        mProgress = (ProgressBar) layout.findViewById(R.id.progress);

        mListView = (AbsListView) layout.findViewById(R.id.listView);
        mAdapter = new ModuleListAdapter(getActivity(), mCourse, this, this);
        mListView.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mModules == null) {
            mRefreshLayout.setRefreshing(true);
            mProgress.setVisibility(View.VISIBLE);
            mModuleModel.getModules(mModuleResult, mCourse, false);
        } else {
            mProgress.setVisibility(View.GONE);
            mAdapter.updateModules(mModules);
        }
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        mModuleModel.getModules(mModuleResult, mCourse, false);
    }

    @Override
    public void onModuleButtonClicked(Course course, Module module) {
        startModuleActivity(course, module, null);
    }

    @Override
    public void onItemButtonClicked(Course course, Module module, Item item) {
        startModuleActivity(course, module, item);
    }

    private void startModuleActivity(Course course, Module module, Item item) {
        Intent intent = new Intent(getActivity(), ModuleActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(ModuleActivity.ARG_COURSE, course);
        b.putParcelable(ModuleActivity.ARG_MODULE, module);
        b.putParcelable(ModuleActivity.ARG_ITEM, item);
        intent.putExtras(b);
        startActivityForResult(intent, REQUEST_CODE_MODULES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MODULES && resultCode == Activity.RESULT_OK) {
            Module newModule = data.getExtras().getParcelable(ModuleActivity.ARG_MODULE);

            if (mModules != null && mAdapter != null) {
                mModules.set(mModules.indexOf(newModule), newModule);
                mAdapter.updateModules(mModules);
            }
        }
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

}
