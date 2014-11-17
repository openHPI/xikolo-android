package de.xikolo.controller.course;

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
import de.xikolo.entities.Course;
import de.xikolo.entities.Item;
import de.xikolo.entities.Module;
import de.xikolo.model.ItemModel;
import de.xikolo.model.ModuleModel;
import de.xikolo.model.OnModelResponseListener;

public class CourseLearningsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        ModuleListAdapter.OnModuleButtonClickListener, ItemListAdapter.OnItemButtonClickListener {

    public final static String TAG = CourseLearningsFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private static final String KEY_MODULES = "key_modules";

    private AbsListView mListView;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mProgress;

    private boolean mCache;

    private ModuleModel mModuleModel;
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

        mModuleModel = new ModuleModel(getActivity(), jobManager);
        mModuleModel.setRetrieveModulesListener(new OnModelResponseListener<List<Module>>() {
            @Override
            public void onResponse(final List<Module> response) {
                mRefreshLayout.setRefreshing(false);
                mProgress.setVisibility(View.GONE);
                if (response != null) {
                    mAdapter.updateModules(response);
                    mModules = response;

                    for (final Module module : mModules) {
                        if (module.items == null || module.items.size() == 0) {
                            ItemModel itemModel = new ItemModel(getActivity(), jobManager);
                            itemModel.setRetrieveItemsListener(new OnModelResponseListener<List<Item>>() {
                                @Override
                                public void onResponse(final List<Item> response) {
                                    if (response != null) {
                                        module.items = response;
                                        mAdapter.updateModules(mModules);
                                    }
                                }
                            });
                            itemModel.retrieveItems(mCourse.id, module.id, mCache);
                        }
                    }

                }
            }
        });
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
            mCache = true;
            mRefreshLayout.setRefreshing(true);
            mProgress.setVisibility(View.VISIBLE);
            mModuleModel.retrieveModules(mCourse.id, mCache, false);
        } else {
            mProgress.setVisibility(View.GONE);
            mAdapter.updateModules(mModules);
        }
    }

    @Override
    public void onRefresh() {
        mCache = false;
        mRefreshLayout.setRefreshing(true);
        mModuleModel.retrieveModules(mCourse.id, mCache, false);
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
        startActivity(intent);
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
