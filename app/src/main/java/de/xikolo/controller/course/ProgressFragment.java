package de.xikolo.controller.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.BaseFragment;
import de.xikolo.controller.course.adapter.ModuleProgressListAdapter;
import de.xikolo.entities.Course;
import de.xikolo.entities.Module;
import de.xikolo.model.ModuleModel;
import de.xikolo.model.OnModelResponseListener;

public class ProgressFragment extends BaseFragment {

    public static final String TAG = ProgressFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private static final String KEY_MODULES = "key_modules";

    private Course mCourse;
    private List<Module> mModules;

    private ModuleModel mModuleModel;

    private ModuleProgressListAdapter mAdapter;

    private ListView mProgressScrollView;
    private ProgressBar mProgress;

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

        mModuleModel = new ModuleModel(getActivity(), jobManager);
        mModuleModel.setRetrieveModulesListener(new OnModelResponseListener<List<Module>>() {
            @Override
            public void onResponse(final List<Module> response) {
                mProgress.setVisibility(View.GONE);
                if (response != null) {
                    mModules = response;
                    mAdapter.updateModules(response);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_progress, container, false);

        mProgressScrollView = (ListView) layout.findViewById(R.id.listView);
        mProgress = (ProgressBar) layout.findViewById(R.id.progress);

        mAdapter = new ModuleProgressListAdapter(getActivity());
        mProgressScrollView.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mModules == null) {
            mProgress.setVisibility(View.VISIBLE);
            mModuleModel.retrieveModules(mCourse.id, false, true);
        } else {
            mProgress.setVisibility(View.GONE);
            mAdapter.updateModules(mModules);
        }
    }

}