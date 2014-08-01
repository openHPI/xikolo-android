package de.xikolo.controller.course;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.course.adapter.ModuleProgressListAdapter;
import de.xikolo.manager.ModuleManager;
import de.xikolo.model.Course;
import de.xikolo.model.Module;
import de.xikolo.util.ProgressBarAnimator;

public class ProgressFragment extends Fragment {

    public static final String TAG = ProgressFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private static final String KEY_MODULES = "key_modules";

    private Course mCourse;
    private List<Module> mModules;

    private ModuleManager mModuleManager;

    private ModuleProgressListAdapter mAdapter;

    private LinearLayout mProgressView;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_progress, container, false);

        mProgressView = (LinearLayout) layout.findViewById(R.id.listView);
        mProgress = (ProgressBar) layout.findViewById(R.id.progress);

        mAdapter = new ModuleProgressListAdapter(getActivity());

        mModuleManager = new ModuleManager(getActivity()) {
            @Override
            public void onModulesRequestReceived(List<Module> modules) {
                if (modules != null) {
                    mProgress.setVisibility(View.GONE);
                    mModules = modules;
                    mAdapter.updateModules(modules);
                    addProgressViews();
                }
            }

            @Override
            public void onModulesRequestCancelled() {

            }
        };

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mModules == null) {
            mProgress.setVisibility(View.VISIBLE);
            mModuleManager.requestModules(mCourse, false, true);
        } else {
            mProgress.setVisibility(View.GONE);
            mAdapter.updateModules(mModules);
            addProgressViews();
        }
    }

    private void addProgressViews() {
        mProgressView.removeAllViews();

        int count_visited = 0;
        int count_available = 0;

        int self_tests_points_scored = 0;
        int self_tests_points_possible = 0;

        int assignments_points_scored = 0;
        int assignments_points_possible = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View v = mAdapter.getView(i, null, null);

            mProgressView.addView(v);

            ProgressBar progress1 = (ProgressBar) v.findViewById(R.id.progress1);
            TextView label1 = (TextView) v.findViewById(R.id.textPercentage1);
            ProgressBar progress2 = (ProgressBar) v.findViewById(R.id.progress2);
            TextView label2 = (TextView) v.findViewById(R.id.textPercentage2);
            ProgressBar progress3 = (ProgressBar) v.findViewById(R.id.progress3);
            TextView label3 = (TextView) v.findViewById(R.id.textPercentage3);

            Module module = (Module) mAdapter.getItem(i);

//            module.progress.self_tests.points_scored = 1;
//            module.progress.self_tests.points_possible = 2;
//            module.progress.assignments.points_scored = 1;
//            module.progress.assignments.points_possible = 2;

            ProgressBarAnimator.start(getActivity(), progress1, label1,
                    module.progress.self_tests.points_scored, module.progress.self_tests.points_possible);

            ProgressBarAnimator.start(getActivity(), progress2, label2,
                    module.progress.assignments.points_scored, module.progress.assignments.points_possible);

            ProgressBarAnimator.start(getActivity(), progress3, label3,
                    module.progress.items.count_visited, module.progress.items.count_available);

            count_visited += module.progress.items.count_visited;
            count_available += module.progress.items.count_available;

            self_tests_points_scored += module.progress.self_tests.points_scored;
            self_tests_points_possible += module.progress.self_tests.points_possible;

            assignments_points_scored += module.progress.assignments.points_scored;
            assignments_points_possible += module.progress.assignments.points_possible;
        }

        View total = LayoutInflater.from(getActivity()).inflate(R.layout.item_module_progress, null);
        mProgressView.addView(total);

        View separator = total.findViewById(R.id.viewSeparator);
        separator.setVisibility(View.VISIBLE);

        TextView title = (TextView) total.findViewById(R.id.textTitle);
        TextView count1 = (TextView) total.findViewById(R.id.textCount1);
        TextView count2 = (TextView) total.findViewById(R.id.textCount2);
        TextView count3 = (TextView) total.findViewById(R.id.textCount3);
        ProgressBar progress1 = (ProgressBar) total.findViewById(R.id.progress1);
        ProgressBar progress2 = (ProgressBar) total.findViewById(R.id.progress2);
        ProgressBar progress3 = (ProgressBar) total.findViewById(R.id.progress3);
        TextView label1 = (TextView) total.findViewById(R.id.textPercentage1);
        TextView label2 = (TextView) total.findViewById(R.id.textPercentage2);
        TextView label3 = (TextView) total.findViewById(R.id.textPercentage3);

        title.setText(getString(R.string.total));

        count1.setText(self_tests_points_scored + " " +
                getString(R.string.of) + " " + self_tests_points_possible + " " +
                getString(R.string.self_tests) + " " + getString(R.string.points));

        count2.setText(assignments_points_scored + " " +
                getString(R.string.of) + " " + assignments_points_possible + " " +
                getString(R.string.assignments) + " " + getString(R.string.points));

        count3.setText(count_visited + " " +
                getString(R.string.of) + " " + count_available + " " +
                getString(R.string.visited));

        ProgressBarAnimator.start(getActivity(), progress1, label1,
                self_tests_points_scored, self_tests_points_possible);

        ProgressBarAnimator.start(getActivity(), progress2, label2,
                assignments_points_scored, assignments_points_possible);

        ProgressBarAnimator.start(getActivity(), progress3, label3,
                count_visited, count_available);
    }

}
