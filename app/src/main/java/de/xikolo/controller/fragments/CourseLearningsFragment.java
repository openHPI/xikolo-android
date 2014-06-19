package de.xikolo.controller.fragments;

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

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.ModuleActivity;
import de.xikolo.controller.fragments.adapter.ItemListAdapter;
import de.xikolo.controller.fragments.adapter.ModuleListAdapter;
import de.xikolo.manager.ItemManager;
import de.xikolo.manager.ModuleManager;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.Module;

public class CourseLearningsFragment extends ContentFragment implements SwipeRefreshLayout.OnRefreshListener,
        ModuleListAdapter.OnModuleButtonClickListener, ItemListAdapter.OnItemButtonClickListener {

    public final static String TAG = CourseLearningsFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private static final String KEY_MODULES = "key_modules";

    private AbsListView mListView;
    private SwipeRefreshLayout mRefreshLayout;

    private boolean mCache;

    private ModuleManager mModuleManager;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_learnings, container, false);

        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorScheme(
                R.color.red,
                R.color.orange,
                R.color.red,
                R.color.orange);
        mRefreshLayout.setOnRefreshListener(this);

        mListView = (AbsListView) layout.findViewById(R.id.listView);
        mAdapter = new ModuleListAdapter(getActivity(), mCourse, this, this);
        mListView.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mModuleManager = new ModuleManager(getActivity()) {
            @Override
            public void onModulesRequestReceived(final List<Module> modules) {
                mRefreshLayout.setRefreshing(false);
                if (modules != null) {
                    mAdapter.updateModules(modules);
                    mModules = modules;
                    for (final Module module : mModules) {
                        if (module.items == null || module.items.size() == 0) {
                            ItemManager itemManager = new ItemManager(getActivity()) {
                                @Override
                                public void onItemsRequestReceived(List<Item> items) {
                                    if (items != null) {
                                        module.items = items;
                                        mAdapter.updateModules(modules);
                                    }
                                }

                                @Override
                                public void onItemsRequestCancelled() {
                                }
                            };
                            itemManager.requestItems(mCourse, module, mCache);
                        }
                    }
                }
            }

            @Override
            public void onModulesRequestCancelled() {
                mRefreshLayout.setRefreshing(true);
            }
        };

        if (mModules == null) {
            mCache = true;
            mRefreshLayout.setRefreshing(true);
            mModuleManager.requestModules(mCourse, mCache);
        } else {
            mAdapter.updateModules(mModules);
        }
    }

    @Override
    public void onRefresh() {
        mCache = false;
        mRefreshLayout.setRefreshing(true);
        mModuleManager.requestModules(mCourse, mCache);
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
        if (!mCallback.isDrawerOpen())
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
